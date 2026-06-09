package com.nabd.ai.local.autonomy.safety

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExecutionGuardrailsTest {

    @Test
    fun `enforces max steps limit`() {
        val guardrails = ExecutionGuardrails(maxAutonomousSteps = 2)
        guardrails.recordStep()
        assertNull(guardrails.checkGuardrails())
        
        guardrails.recordStep()
        assertNotNull(guardrails.checkGuardrails()) // Reached 2
    }

    @Test
    fun `enforces consecutive failures limit`() {
        val guardrails = ExecutionGuardrails(maxConsecutiveFailures = 2)
        guardrails.recordFailure()
        assertNull(guardrails.checkGuardrails())
        
        guardrails.recordFailure()
        assertNotNull(guardrails.checkGuardrails()) // Reached 2 failures
    }
    
    @Test
    fun `resets consecutive failures on success`() {
        val guardrails = ExecutionGuardrails(maxConsecutiveFailures = 2)
        guardrails.recordFailure()
        guardrails.recordSuccess()
        guardrails.recordFailure()
        assertNull(guardrails.checkGuardrails()) // Did not reach 2 consecutively
    }
}
