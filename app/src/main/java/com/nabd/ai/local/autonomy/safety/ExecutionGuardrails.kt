package com.nabd.ai.local.autonomy.safety

class ExecutionGuardrails(
    private val maxAutonomousSteps: Int = 25,
    private val maxReplans: Int = 5,
    private val maxConsecutiveFailures: Int = 3
) {
    var currentSteps = 0
        private set
    var currentReplans = 0
        private set
    var consecutiveFailures = 0
        private set

    fun recordStep() {
        currentSteps++
    }

    fun recordFailure() {
        consecutiveFailures++
    }
    
    fun recordSuccess() {
        consecutiveFailures = 0
    }

    fun recordReplan() {
        currentReplans++
    }

    fun checkGuardrails(): String? {
        if (currentSteps >= maxAutonomousSteps) {
            return "Safety limit reached: Maximum autonomous steps ($maxAutonomousSteps) exceeded."
        }
        if (currentReplans >= maxReplans) {
            return "Safety limit reached: Maximum replanning attempts ($maxReplans) exceeded."
        }
        if (consecutiveFailures >= maxConsecutiveFailures) {
            return "Safety limit reached: Maximum consecutive failures ($maxConsecutiveFailures) exceeded."
        }
        return null
    }

    fun reset() {
        currentSteps = 0
        currentReplans = 0
        consecutiveFailures = 0
    }
}
