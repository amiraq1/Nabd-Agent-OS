package com.nabd.ai.local.autonomy.improvement

import java.util.UUID

/**
 * ImprovementRecommendation: A suggested change to enhance agent performance.
 */
data class ImprovementRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val targetArea: String, // e.g., "Planner Prompt", "Tool Selection"
    val description: String,
    val rationale: String,
    val suggestedChange: String,
    var status: RecommendationStatus = RecommendationStatus.PENDING
)

enum class RecommendationStatus { PENDING, APPROVED, REJECTED, IMPLEMENTED }

/**
 * ContinuousImprovementEngine: Analyzes performance metrics and generates recommendations.
 */
class ContinuousImprovementEngine {
    private val recommendations = mutableListOf<ImprovementRecommendation>()

    /**
     * Analyzes execution history to find optimization opportunities.
     */
    fun analyzePerformance(
        failureRate: Float,
        replanningFrequency: Float,
        avgLatencyMs: Long
    ): List<ImprovementRecommendation> {
        val newRecommendations = mutableListOf<ImprovementRecommendation>()

        if (replanningFrequency > 0.3f) {
            newRecommendations.add(ImprovementRecommendation(
                targetArea = "Planner Strategy",
                description = "High replanning frequency detected.",
                rationale = "The agent is frequently needing to adjust its plans, suggesting initial plans are too optimistic or vague.",
                suggestedChange = "Enhance the PlannerAgent prompt to include more edge-case considerations and tool constraints."
            ))
        }

        if (avgLatencyMs > 60000) { // > 1 minute
            newRecommendations.add(ImprovementRecommendation(
                targetArea = "Model Selection",
                description = "Average latency is high.",
                rationale = "Cloud models or large local models are taking too long for simple steps.",
                suggestedChange = "Consider using a smaller model (e.g., Llama-3-8B) for initial task decomposition and tool-call formatting."
            ))
        }

        recommendations.addAll(newRecommendations)
        return newRecommendations
    }

    fun getPendingRecommendations(): List<ImprovementRecommendation> {
        return recommendations.filter { it.status == RecommendationStatus.PENDING }
    }

    fun resolveRecommendation(id: String, status: RecommendationStatus) {
        recommendations.find { it.id == id }?.status = status
    }
}
