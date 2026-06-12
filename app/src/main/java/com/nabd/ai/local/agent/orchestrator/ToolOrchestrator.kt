package com.nabd.ai.local.agent.orchestrator

import com.nabd.ai.local.agent.AgentExecutionState
import com.nabd.ai.local.agent.ToolRegistry
import com.nabd.ai.local.agent.approval.ApprovalManager
import com.nabd.ai.local.agent.parser.ToolCallParser
import com.nabd.ai.local.agent.parser.ToolCallResult
import com.nabd.ai.local.agent.tools.filesystem.WriteFileTool
import com.nabd.ai.local.agent.trace.ExecutionTrace
import com.nabd.ai.local.agent.trace.TraceEntry
import com.nabd.ai.local.agent.trace.TraceType
import com.nabd.ai.local.autonomy.safety.ToolSandbox
import com.nabd.ai.local.engine.LlmProvider
import com.nabd.ai.local.engine.GenerationRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

class ToolOrchestrator(
    private val provider: LlmProvider,
    private val toolRegistry: ToolRegistry,
    private val approvalManager: ApprovalManager,
    private val sandbox: ToolSandbox? = null
) {
    private val _state = MutableStateFlow<AgentExecutionState>(AgentExecutionState.Idle)
    val state: StateFlow<AgentExecutionState> = _state.asStateFlow()

    private val _trace = MutableStateFlow(ExecutionTrace(conversationId = "default"))
    val trace: StateFlow<ExecutionTrace> = _trace.asStateFlow()

    private var approvalDeferred: CompletableDeferred<Boolean>? = null
    private val parser = ToolCallParser()
    private val maxIterations = 10

    private val jsonGrammar = """
        root ::= "{" ws "\"tool\"" ws ":" ws string ws "," ws "\"parameters\"" ws ":" ws object "}"
        value ::= object | array | string | number | "true" | "false" | "null"
        object ::= "{" ws (string ws ":" ws value (ws "," ws string ws ":" ws value)*)? ws "}"
        array ::= "[" ws (value (ws "," ws value)*)? ws "]"
        string ::= "\"" ([^"\\] | "\\" (["\\/bfnrt] | "u" [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]))* "\""
        number ::= "-"? ([0-9] | [1-9] [0-9]*) ("." [0-9]+)? ([eE] [-+]? [0-9]+)?
        ws ::= [ \t\n]*
    """.trimIndent()

    suspend fun runLoop(userRequest: String) {
        addTrace(TraceType.PROMPT, userRequest)
        var iteration = 0
        var currentContext = userRequest
        
        while (iteration < maxIterations) {
            iteration++
            _state.value = AgentExecutionState.Thinking
            addTrace(TraceType.THOUGHT, "Iteration ${'$'}iteration: Generating next action...")

            var generatedJson = ""
            try {
                generatedJson = provider.generateResponse(
                    GenerationRequest(
                        prompt = currentContext,
                        grammar = jsonGrammar
                    )
                )
                addTrace(TraceType.THOUGHT, "Generated JSON: ${'$'}generatedJson")
            } catch (e: Exception) {
                addTrace(TraceType.ERROR, "Generation failed: ${'$'}{e.message}")
                _state.value = AgentExecutionState.Failed("Generation failed: ${'$'}{e.message}")
                return
            }

            val parseResult = parser.parse(generatedJson)
            when (parseResult) {
                is ToolCallResult.Success -> {
                    val toolCall = parseResult.toolCall
                    
                    if (toolCall.tool == "finish") {
                        addTrace(TraceType.REFLECTION, "Agent chose to finish.")
                        _state.value = AgentExecutionState.Completed
                        return
                    }

                    val observation = dispatchTool(toolCall.tool, JSONObject(toolCall.parameters).toString())
                    
                    val trimmedObservation = if (observation.length > 2000) observation.substring(0, 1997) + "..." else observation
                    currentContext += "\n\nTool Call: ${'$'}generatedJson\nObservation: ${'$'}trimmedObservation\n\nNext Action:"
                    
                    if (currentContext.length > 8000) {
                        currentContext = currentContext.substring(currentContext.length - 8000)
                    }
                }
                is ToolCallResult.Error -> {
                    addTrace(TraceType.ERROR, "Parse error: ${'$'}{parseResult.reason}")
                    currentContext += "\n\nTool Call: ${'$'}generatedJson\nObservation: Parsing Error: ${'$'}{parseResult.reason}. Please try again.\n\nNext Action:"
                }
            }
        }
        
        if (iteration >= maxIterations) {
            addTrace(TraceType.ERROR, "Max iterations reached.")
            _state.value = AgentExecutionState.Failed("Max iterations reached.")
        } else {
            _state.value = AgentExecutionState.Completed
        }
    }

    private suspend fun dispatchTool(name: String, params: String): String {
        // 1. Sandbox Validation
        sandbox?.let {
            if (!it.validateCommand(params)) {
                it.auditLog(name, params, allowed = false)
                return "Error: Command rejected by sandbox for security reasons."
            }
            it.auditLog(name, params, allowed = true)
        }

        val tool = toolRegistry.getTool(name) ?: run {
            val errorMsg = "Error: Tool not found: ${'$'}name"
            addTrace(TraceType.ERROR, errorMsg)
            return errorMsg
        }

        if (tool.requiresHumanApproval) {
            val preview = when (tool) {
                is WriteFileTool -> tool.generatePreview(params)
                is com.nabd.ai.local.agent.tools.intelligence.CrossFileRefactorTool -> tool.generatePreview(params)
                else -> "Destructive action requested."
            }
            _state.value = AgentExecutionState.WaitingForApproval(name, params)
            addTrace(TraceType.APPROVAL_REQUEST, "Requires approval for ${'$'}name")
            
            val request = approvalManager.requestApproval(name, "Agent wants to execute ${'$'}name", preview)
            val approved = waitOnApproval()
            approvalManager.resolveApproval(request.id)
            
            addTrace(TraceType.APPROVAL_RESPONSE, if (approved) "Approved" else "Rejected")
            
            if (!approved) {
                _state.value = AgentExecutionState.Idle
                return "Observation: Tool execution rejected by user. Aborting action."
            }
        }

        _state.value = AgentExecutionState.ExecutingTool(name)
        addTrace(TraceType.TOOL_CALL, "Calling ${'$'}name with ${'$'}params")

        return try {
            val result = tool.execute(params)
            val observation = result.getOrElse { "Error: ${it.message ?: "Unknown tool error"}" }
            addTrace(TraceType.TOOL_RESULT, observation)
            _state.value = AgentExecutionState.Observing
            observation
        } catch (e: SecurityException) {
            val errorMsg = "Security Exception: ${'$'}{e.message}. You cannot access files outside the workspace."
            addTrace(TraceType.ERROR, errorMsg)
            errorMsg
        } catch (e: Exception) {
            val errorMsg = "Error during execution: ${'$'}{e.message}"
            addTrace(TraceType.ERROR, errorMsg)
            errorMsg
        }
    }

    fun approveTool(approved: Boolean) {
        approvalDeferred?.complete(approved)
    }

    private suspend fun waitOnApproval(): Boolean {
        approvalDeferred = CompletableDeferred()
        val result = kotlinx.coroutines.withTimeoutOrNull(300_000L) {
            approvalDeferred!!.await()
        }
        return result ?: false
    }

    private fun addTrace(type: TraceType, content: String) {
        _trace.update { current ->
            current.copy(entries = current.entries + TraceEntry(type, content))
        }
    }
}
