package com.nabd.ai.local.autonomy.session

import com.nabd.ai.local.autonomy.state.PlanStateManager
import java.util.UUID

class SessionManager(
    private val stateManager: PlanStateManager
) {
    var currentSession: AgentSession? = null
        private set

    fun createSession(goal: String): AgentSession {
        val session = AgentSession(
            sessionId = UUID.randomUUID().toString(),
            activeGoal = goal,
            currentPlan = null
        )
        currentSession = session
        save()
        return session
    }

    fun resumeLatestSession(): AgentSession? {
        val latestId = stateManager.getLatestSessionId() ?: return null
        val session = stateManager.loadSession(latestId)
        if (session != null) {
            currentSession = session
            return session
        }
        return null
    }

    fun save() {
        currentSession?.let { stateManager.saveSession(it) }
    }
}
