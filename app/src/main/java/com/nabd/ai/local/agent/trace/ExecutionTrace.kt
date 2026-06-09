package com.nabd.ai.local.agent.trace

import java.util.UUID

/**
 * ExecutionTrace: A persistent record of an agent's reasoning and action sequence.
 */
data class ExecutionTrace(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val entries: List<TraceEntry> = emptyList()
)

/**
 * TraceEntry: A single step in the execution trace.
 */
data class TraceEntry(
    val type: TraceType,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TraceType {
    PROMPT,
    THOUGHT,
    TOOL_CALL,
    TOOL_RESULT,
    ERROR,
    REFLECTION,
    APPROVAL_REQUEST,
    APPROVAL_RESPONSE
}
