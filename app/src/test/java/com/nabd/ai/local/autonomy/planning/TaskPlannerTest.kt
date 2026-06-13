package com.nabd.ai.local.autonomy.planning

import com.nabd.ai.local.engine.LlmProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TaskPlannerTest {

    @Test
    fun `parses valid execution plan from LLM output`() = runTest {
        val provider = mockk<LlmProvider>()
        val jsonOutput = """
            {
              "steps": [
                {
                  "objective": "Step 1",
                  "rationale": "Reason 1",
                  "definitionOfDone": "Done 1",
                  "requiredTools": ["tool1"],
                  "dependencies": []
                },
                {
                  "objective": "Step 2",
                  "rationale": "Reason 2",
                  "definitionOfDone": "Done 2",
                  "requiredTools": ["tool2"],
                  "dependencies": []
                }
              ]
            }
        """.trimIndent()
        
        coEvery { provider.generateResponse(any()) } returns jsonOutput
        
        val planner = TaskPlanner(provider)
        val plan = planner.generatePlan("Goal", listOf("tool1", "tool2"))
        
        assertEquals("Goal", plan.originalGoal)
        assertEquals(2, plan.steps.size)
        assertEquals("Step 1", plan.steps[0].objective)
        assertEquals("Step 2", plan.steps[1].objective)
        
        // Ensure implicit dependencies are injected for step 2
        assertEquals(1, plan.steps[1].dependencies.size)
        assertEquals(plan.steps[0].id, plan.steps[1].dependencies[0])
    }
}
