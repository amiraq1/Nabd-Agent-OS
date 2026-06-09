package com.nabd.ai.local.autonomy.session

import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.history.TimelineEvent

data class AgentSession(
    val sessionId: String,
    val activeGoal: String,
    var currentPlan: ExecutionPlan?,
    val timeline: MutableList<TimelineEvent> = mutableListOf(),
    var isPaused: Boolean = false
)
