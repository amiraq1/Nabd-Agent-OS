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
        try {
            val root = org.json.JSONObject(json)
            val stepsArray = root.getJSONArray("steps")
            val stepsList = mutableListOf<PlanStep>()

            for (i in 0 until stepsArray.length()) {
                val stepObj = stepsArray.getJSONObject(i)

                val reqToolsArr = stepObj.optJSONArray("requiredTools") ?: org.json.JSONArray()
                val reqTools = List(reqToolsArr.length()) { reqToolsArr.getString(it) }

                val depsArr = stepObj.optJSONArray("dependencies") ?: org.json.JSONArray()
                val deps = List(depsArr.length()) { depsArr.getString(it) }

                stepsList.add(PlanStep(
                    id = UUID.randomUUID(),
                    toolName = stepObj.optString("toolName", reqTools.firstOrNull() ?: "unknown"),
                    arguments = mapOf("objective" to (stepObj.optString("objective", "") as Any)),
                    dependencies = if (i > 0 && deps.isEmpty()) {
                        listOf(stepsList[i - 1].id)
                    } else {
                        deps.mapNotNull { depStr ->
                            try { UUID.fromString(depStr) } catch (_: Exception) {
                                // If LLM returned indices, map to sequential deps
                                if (i > 0) stepsList[i - 1].id else null
                            }
                        }
                    },
                    requiredPermissions = emptyList()
                ))
            }

            return ExecutionPlan(UUID.randomUUID(), stepsList)
        } catch (e: Exception) {
            android.util.Log.w("ReplanningEngine", "Failed to parse correction plan JSON, creating fallback", e)
            // Fallback: single-step plan with the raw LLM response as the objective
            return ExecutionPlan(
                UUID.randomUUID(),
                listOf(PlanStep(
                    id = UUID.randomUUID(),
                    toolName = "finish",
                    arguments = mapOf("rawResponse" to json),
                    dependencies = emptyList(),
                    requiredPermissions = emptyList()
                ))
            )
        }
    }
}
