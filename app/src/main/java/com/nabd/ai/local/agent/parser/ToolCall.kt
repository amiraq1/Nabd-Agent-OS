package com.nabd.ai.local.agent.parser

data class ToolCall(
    val tool: String,
    val parameters: Map<String, Any>
)
