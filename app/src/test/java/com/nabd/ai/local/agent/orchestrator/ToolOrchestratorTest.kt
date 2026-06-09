package com.nabd.ai.local.agent.orchestrator

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRegistry
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.engine.LlmProvider
import com.nabd.ai.local.engine.EngineState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ToolOrchestratorTest {

    @Test
    fun `orchestrator runs loop and calls tool`() = runTest {
        val provider = mockk<LlmProvider>()
        val registry = ToolRegistry()
        
        val dummyTool = object : ToolDefinition {
            override val name = "dummy"
            override val description = "Dummy tool"
            override val jsonSchema = "{}"
            override val category = ToolCategory.UTILITY
            override val riskLevel = ToolRiskLevel.SAFE
            override suspend fun execute(params: String): Result<String> {
                return Result.success("dummy observation")
            }
        }
        registry.register(dummyTool)
        
        val engineState = MutableStateFlow<EngineState>(EngineState.Loaded)
        every { provider.state } returns engineState
        
        // Mock the LLM to output a valid JSON tool call first, then a finish tool call.
        // Since the orchestrator loops, we need to return flows for consecutive calls.
        coEvery { provider.generateText(any(), any()) } returnsMany listOf(
            flowOf("""{"tool": "dummy", "parameters": {}}"""),
            flowOf("""{"tool": "finish", "parameters": {}}""")
        )

        val approvalManager = mockk<com.nabd.ai.local.agent.approval.ApprovalManager>(relaxed = true)
        val orchestrator = ToolOrchestrator(provider, registry, approvalManager)
        
        orchestrator.runLoop("Do something")
        
        val trace = orchestrator.trace.value.entries
        assertTrue(trace.any { it.content.contains("dummy observation") })
        assertTrue(trace.any { it.content.contains("Agent chose to finish") })
    }
}
