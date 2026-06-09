package com.nabd.ai.local.autonomy.replanning

import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.autonomy.reflection.CorrectionStep
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class ReplanningManagerTest {

    @Test
    fun `inserts corrections before failed step and updates dependencies`() {
        val step1 = PlanStep("id1", "Step 1", "R", "D", emptyList(), emptyList())
        val failedStep = PlanStep("id2", "Step 2", "R", "D", emptyList(), listOf("id1"), state = com.nabd.ai.local.autonomy.planning.StepState.FAILED)
        val step3 = PlanStep("id3", "Step 3", "R", "D", emptyList(), listOf("id2"))
        
        val plan = ExecutionPlan("plan1", "Goal", mutableListOf(step1, failedStep, step3), false, true)
        
        val manager = ReplanningManager()
        val corrections = listOf(CorrectionStep("Fix issue", emptyList()))
        
        val newPlan = manager.insertCorrections(plan, "id2", corrections)
        
        assertFalse(newPlan.isFailed)
        assertEquals(4, newPlan.steps.size)
        
        assertEquals("id1", newPlan.steps[0].id)
        
        val correctionStep = newPlan.steps[1]
        assertTrue(correctionStep.id.startsWith("corr_"))
        assertEquals("Fix issue", correctionStep.objective)
        
        val updatedFailedStep = newPlan.steps[2]
        assertEquals("id2", updatedFailedStep.id)
        assertEquals(com.nabd.ai.local.autonomy.planning.StepState.PENDING, updatedFailedStep.state)
        assertEquals(correctionStep.id, updatedFailedStep.dependencies[0])
    }
}
