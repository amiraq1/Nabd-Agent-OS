package com.nabd.ai.local.autonomy.history

import java.util.UUID

enum class EventType {
    PLAN_CREATED,
    STEP_STARTED,
    STEP_COMPLETED,
    STEP_FAILED,
    TOOL_EXECUTED,
    APPROVAL_REQUESTED,
    REFLECTION_EVALUATED,
    REPLAN_TRIGGERED,
    PAUSED,
    RESUMED,
    CANCELLED
}

data class TimelineEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: EventType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ExecutionTimeline {
    private val _events = mutableListOf<TimelineEvent>()
    val events: List<TimelineEvent> get() = _events.toList()

    fun addEvent(type: EventType, description: String) {
        _events.add(TimelineEvent(type = type, description = description))
    }

    fun load(existingEvents: List<TimelineEvent>) {
        _events.clear()
        _events.addAll(existingEvents)
    }
}
