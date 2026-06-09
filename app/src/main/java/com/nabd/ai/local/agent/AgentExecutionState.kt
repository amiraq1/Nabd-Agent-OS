package com.nabd.ai.local.agent

/**
 * AgentExecutionState: Explicit lifecycle phases of the Agent's reasoning loop.
 */
sealed interface AgentExecutionState {
    data object Idle : AgentExecutionState
    data object Thinking : AgentExecutionState
    data class CallingTool(val toolName: String) : AgentExecutionState
    data class WaitingForApproval(val toolName: String, val params: String) : AgentExecutionState
    data class ExecutingTool(val toolName: String) : AgentExecutionState
    data object Observing : AgentExecutionState
    data object Reflecting : AgentExecutionState
    data object Completed : AgentExecutionState
    data class Failed(val error: String) : AgentExecutionState
}
