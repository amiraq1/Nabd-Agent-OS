package com.nabd.ai.local.autonomy.state

import com.nabd.ai.local.autonomy.session.AgentSession
import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.planning.PlanStep
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class PlanStateManagerTest {

    @Test
    fun `saves and loads session state correctly`() {
        val tempDir = Files.createTempDirectory("state_test").toFile()
        val manager = PlanStateManager(tempDir)
        
        val step = PlanStep("s1", "Obj", "Rat", "DoD", listOf("toolA"), listOf("dep1"))
        val plan = ExecutionPlan("p1", "Goal", mutableListOf(step))
        val session = AgentSession("sess1", "Goal", plan)
        
        manager.saveSession(session)
        
        val loaded = manager.loadSession("sess1")
        assertNotNull(loaded)
        assertEquals("sess1", loaded?.sessionId)
        assertEquals("Goal", loaded?.activeGoal)
        assertEquals("p1", loaded?.currentPlan?.id)
        assertEquals(1, loaded?.currentPlan?.steps?.size)
        assertEquals("s1", loaded?.currentPlan?.steps?.get(0)?.id)
        assertEquals("toolA", loaded?.currentPlan?.steps?.get(0)?.requiredTools?.get(0))
        assertEquals("dep1", loaded?.currentPlan?.steps?.get(0)?.dependencies?.get(0))
    }
}
