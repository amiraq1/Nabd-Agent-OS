package com.nabd.ai.local.autonomy.session

import com.nabd.ai.local.autonomy.state.PlanStateManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SessionManagerTest {

    @Test
    fun `creates session and saves to state manager`() {
        val stateManager = mockk<PlanStateManager>(relaxed = true)
        val sessionManager = SessionManager(stateManager)
        
        val session = sessionManager.createSession("Test goal")
        
        assertNotNull(session)
        assertEquals("Test goal", session.activeGoal)
        assertEquals(session, sessionManager.currentSession)
        
        verify { stateManager.saveSession(session) }
    }

    @Test
    fun `resumes latest session correctly`() {
        val stateManager = mockk<PlanStateManager>()
        val dummySession = AgentSession("id1", "Old Goal", null)
        
        every { stateManager.getLatestSessionId() } returns "id1"
        every { stateManager.loadSession("id1") } returns dummySession
        
        val sessionManager = SessionManager(stateManager)
        val resumed = sessionManager.resumeLatestSession()
        
        assertNotNull(resumed)
        assertEquals("id1", resumed?.sessionId)
        assertEquals(dummySession, sessionManager.currentSession)
    }
}
