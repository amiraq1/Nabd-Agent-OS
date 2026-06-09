package com.nabd.ai.local.autonomy.reflection

import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.engine.LlmProvider
import org.json.JSONObject

class ReflectionEngine(
    private val provider: LlmProvider
) {
    private val reflectionGrammar = """
        root ::= "{" ws "\"isSuccess\"" ws ":" ws ( "true" | "false" ) ws "," ws "\"reasoning\"" ws ":" ws string ws "," ws "\"suggestedCorrections\"" ws ":" ws "[" ws (correction (ws "," ws correction)*)? ws "]" ws "}"
        correction ::= "{" ws "\"actionToTake\"" ws ":" ws string ws "," ws "\"newToolsRequired\"" ws ":" ws string_array ws "}"
        string_array ::= "[" ws (string (ws "," ws string)*)? ws "]"
        string ::= "\"" ([^"\\] | "\\" (["\\/bfnrt] | "u" [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]))* "\""
        ws ::= [ \t\n]*
    """.trimIndent()

    suspend fun evaluateStep(step: PlanStep, finalObservation: String): EvaluationResult {
        val prompt = """
            You are a strict QA system evaluating an autonomous agent.
            Step Objective: ${step.objective}
            Definition of Done: ${step.definitionOfDone}
            Agent's Observation/Result: $finalObservation
            
            Did the agent successfully meet the Definition of Done?
            If no, provide exactly what needs to be done to correct it.
            
            Return ONLY a valid JSON object matching the strict schema.
        """.trimIndent()

        val tokens = mutableListOf<String>()
        try {
            provider.generateText(prompt, reflectionGrammar).collect { tokens.add(it) }
            val jsonStr = tokens.joinToString("")
            val root = JSONObject(jsonStr)
            
            val isSuccess = root.getBoolean("isSuccess")
            val reasoning = root.getString("reasoning")
            val corrections = mutableListOf<CorrectionStep>()
            
            if (root.has("suggestedCorrections")) {
                val arr = root.getJSONArray("suggestedCorrections")
                for (i in 0 until arr.length()) {
                    val cObj = arr.getJSONObject(i)
                    val toolsArr = cObj.getJSONArray("newToolsRequired")
                    val tools = List(toolsArr.length()) { toolsArr.getString(it) }
                    corrections.add(CorrectionStep(cObj.getString("actionToTake"), tools))
                }
            }
            
            return EvaluationResult(isSuccess, reasoning, corrections)
        } catch (e: Exception) {
            // Safe fallback if parsing fails or generation crashes
            return EvaluationResult(
                isSuccess = !finalObservation.contains("Error:", ignoreCase = true), 
                reasoning = "Fallback evaluation due to parsing failure. Assuming success unless observation contains 'Error'.",
                suggestedCorrections = emptyList()
            )
        }
    }
}
