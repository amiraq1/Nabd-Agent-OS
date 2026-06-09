package com.nabd.ai.local.autonomy.planning

enum class StepState {
    PENDING, READY, IN_PROGRESS, COMPLETED, FAILED, BLOCKED, CANCELLED
}

data class PlanStep(
    val id: String,
    val objective: String,
    val rationale: String,
    val definitionOfDone: String,
    val requiredTools: List<String>,
    val dependencies: List<String>, // IDs of other steps that must complete first
    var state: StepState = StepState.PENDING,
    var observation: String? = null,
    var error: String? = null
) {
    fun canStart(allSteps: List<PlanStep>): Boolean {
        if (state != StepState.PENDING && state != StepState.READY) return false
        val unmetDependencies = dependencies.mapNotNull { depId -> 
            allSteps.find { it.id == depId }
        }.filter { it.state != StepState.COMPLETED }
        
        return unmetDependencies.isEmpty()
    }
}
