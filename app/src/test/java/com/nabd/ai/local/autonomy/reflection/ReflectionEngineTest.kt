package com.nabd.ai.local.autonomy.reflection

import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.engine.LlmProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReflectionEngineTest {

    @Test
    fun `evaluates step as failure and extracts corrections`() = runTest {
        val provider = mockk<LlmProvider>()
        val jsonOutput = """
            {
              "isSuccess": false,
              "reasoning": "Failed to compile",
              "suggestedCorrections": [
                {
                  "actionToTake": "Fix syntax error",
                  "newToolsRequired": ["edit_file"]
                }
              ]
            }
        """.trimIndent()
        
        coEvery { provider.generateResponse(any()) } returns jsonOutput
        
        val engine = ReflectionEngine(provider)
        val step = PlanStep("1", "obj", "rat", "dod", emptyList(), emptyList())
        
        val result = engine.evaluateStep(step, "Error: missing semicolon")
        
        assertFalse(result.isSuccess)
        assertEquals("Failed to compile", result.reasoning)
        assertEquals(1, result.suggestedCorrections.size)
        assertEquals("Fix syntax error", result.suggestedCorrections[0].actionToTake)
    }
}
