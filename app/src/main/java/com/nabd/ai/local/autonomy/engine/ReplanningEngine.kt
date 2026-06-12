package com.nabd.ai.local.autonomy.engine

import com.nabd.ai.local.autonomy.validator.ExecutionPlan
import com.nabd.ai.local.autonomy.validator.PlanStep
import com.nabd.ai.local.engine.GenerationRequest
import java.util.UUID

class ReplanningEngine(
    private val llmProvider: com.nabd.ai.local.engine.LlmProvider
) {
    suspend fun generateCorrectionPlan(
        failedPlan: ExecutionPlan,
        currentStatuses: Map<UUID, StepExecutionStatus>,
        failureReason: String
    ): ExecutionPlan {
        val completedWork = currentStatuses.filter { it.value.state == ExecutionState.COMPLETED }
        val failedStepId = currentStatuses.filter { it.value.state == ExecutionState.FAILED }.keys.firstOrNull()

        // بناء سياق التخطيط لـ LLM لحفظ المجهود السابق
        val prompt = """
            You are the TaskPlanner core of Nabd-Agent-OS.
            An execution plan failed at Step ID: $failedStepId.
            Reason for failure: $failureReason.
            
            Successfully completed steps that MUST be preserved:
            ${completedWork.map { "Step ${it.key} output: ${it.value.output}" }.joinToString("\n")}
            
            Generate a corrective JSON plan that reuses the outputs above and patches the failure to achieve the original goal.
        """.trimIndent()

        // Use the new generateResponse API
        val response = llmProvider.generateResponse(GenerationRequest(prompt = prompt))
        return parsePlanFromJson(response)
    }

    private fun parsePlanFromJson(json: String): ExecutionPlan {
        // كود التحليل والتحويل لنموذج ExecutionPlan الفعلي
        // For now, returning an empty plan as a placeholder for the actual JSON parsing logic
        return ExecutionPlan(UUID.randomUUID(), emptyList())
    }
}
