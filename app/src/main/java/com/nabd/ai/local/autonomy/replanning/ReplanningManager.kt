package com.nabd.ai.local.autonomy.replanning

import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.autonomy.planning.StepState
import com.nabd.ai.local.autonomy.reflection.CorrectionStep
import java.util.UUID

class ReplanningManager {
    fun insertCorrections(plan: ExecutionPlan, failedStepId: String, corrections: List<CorrectionStep>): ExecutionPlan {
        val failedIndex = plan.steps.indexOfFirst { it.id == failedStepId }
        if (failedIndex == -1) return plan

        val newSteps = corrections.mapIndexed { index, correction ->
            PlanStep(
                id = "corr_${UUID.randomUUID().toString().substring(0, 8)}",
                objective = correction.actionToTake,
                rationale = "Correction for failed step ${plan.steps[failedIndex].objective}",
                definitionOfDone = "Correction applied.",
                requiredTools = correction.newToolsRequired,
                dependencies = if (index == 0) emptyList() else listOf("corr_${index-1}") // Simplified linking
            )
        }

        // Link new steps sequentially
        for (i in 1 until newSteps.size) {
            newSteps[i] = newSteps[i].copy(dependencies = listOf(newSteps[i-1].id))
        }

        // The failed step should now depend on the last correction step, and be marked PENDING again
        val updatedFailedStep = plan.steps[failedIndex].copy(
            state = StepState.PENDING,
            dependencies = if (newSteps.isNotEmpty()) listOf(newSteps.last().id) else emptyList(),
            error = null,
            observation = null
        )

        val updatedSteps = plan.steps.toMutableList()
        updatedSteps[failedIndex] = updatedFailedStep
        updatedSteps.addAll(failedIndex, newSteps) // Insert corrections BEFORE the failed step

        return plan.copy(steps = updatedSteps, isFailed = false)
    }
}
