package com.nabd.ai.local.agent.parser

sealed class ToolCallResult {
    data class Success(val toolCall: ToolCall) : ToolCallResult()
    data class Error(val reason: String) : ToolCallResult()
}
