package com.nabd.ai.local.autonomy.reflection

data class EvaluationResult(
    val isSuccess: Boolean,
    val reasoning: String,
    val suggestedCorrections: List<CorrectionStep> = emptyList()
)

data class CorrectionStep(
    val actionToTake: String,
    val newToolsRequired: List<String> = emptyList()
)
