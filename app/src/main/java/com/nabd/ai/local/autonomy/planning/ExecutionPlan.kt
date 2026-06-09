package com.nabd.ai.local.autonomy.planning

data class ExecutionPlan(
    val id: String,
    val originalGoal: String,
    val steps: MutableList<PlanStep>,
    var isCompleted: Boolean = false,
    var isFailed: Boolean = false
) {
    fun getNextExecutableStep(): PlanStep? {
        // Find the first pending/ready step whose dependencies are met
        return steps.firstOrNull { it.canStart(steps) }
    }

    fun updateStepState(stepId: String, newState: StepState, observation: String? = null, error: String? = null) {
        val step = steps.find { it.id == stepId } ?: return
        step.state = newState
        if (observation != null) step.observation = observation
        if (error != null) step.error = error

        checkCompletion()
    }

    private fun checkCompletion() {
        if (steps.all { it.state == StepState.COMPLETED }) {
            isCompleted = true
        } else if (steps.any { it.state == StepState.FAILED || it.state == StepState.CANCELLED }) {
            isFailed = true
        }
    }
}
