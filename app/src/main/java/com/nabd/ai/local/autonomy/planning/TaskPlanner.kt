package com.nabd.ai.local.autonomy.planning

import com.nabd.ai.local.engine.LlmProvider
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlinx.coroutines.flow.toList

class TaskPlanner(
    private val provider: LlmProvider
) {
    private val planningGrammar = """
        root ::= "{" ws "\"steps\"" ws ":" ws "[" ws step (ws "," ws step)* ws "]" ws "}"
        step ::= "{" ws "\"objective\"" ws ":" ws string ws "," ws "\"rationale\"" ws ":" ws string ws "," ws "\"definitionOfDone\"" ws ":" ws string ws "," ws "\"requiredTools\"" ws ":" ws string_array ws "," ws "\"dependencies\"" ws ":" ws string_array ws "}"
        string_array ::= "[" ws (string (ws "," ws string)*)? ws "]"
        string ::= "\"" ([^"\\] | "\\" (["\\/bfnrt] | "u" [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]))* "\""
        ws ::= [ \t\n]*
    """.trimIndent()

    suspend fun generatePlan(goal: String, availableTools: List<String>): ExecutionPlan {
        val prompt = """
            You are a senior software architect. Decompose the following goal into a sequence of actionable steps.
            Available Tools: ${availableTools.joinToString(", ")}
            Goal: $goal
            
            Return ONLY a valid JSON object matching the strict schema.
        """.trimIndent()

        val tokens = mutableListOf<String>()
        provider.generateText(prompt, planningGrammar).collect {
            tokens.add(it)
        }
        val generatedJson = tokens.joinToString("")

        return parsePlanJson(generatedJson, goal)
    }

    private fun parsePlanJson(jsonStr: String, goal: String): ExecutionPlan {
        val stepsList = mutableListOf<PlanStep>()
        try {
            val root = JSONObject(jsonStr)
            val stepsArray = root.getJSONArray("steps")
            
            for (i in 0 until stepsArray.length()) {
                val stepObj = stepsArray.getJSONObject(i)
                val id = "step_${UUID.randomUUID().toString().substring(0, 8)}"
                
                val reqToolsArray = stepObj.getJSONArray("requiredTools")
                val reqTools = List(reqToolsArray.length()) { reqToolsArray.getString(it) }
                
                val depsArray = stepObj.getJSONArray("dependencies")
                val deps = List(depsArray.length()) { depsArray.getString(it) }

                stepsList.add(
                    PlanStep(
                        id = id,
                        objective = stepObj.getString("objective"),
                        rationale = stepObj.getString("rationale"),
                        definitionOfDone = stepObj.getString("definitionOfDone"),
                        requiredTools = reqTools,
                        dependencies = deps // Note: in real parsing, the LLM might hallucinate dependency IDs unless we enforce sequential implicit dependencies or a strict ID naming scheme. 
                                            // For simplicity, we assume linear sequential if dependencies don't exactly match IDs, or the planner assigns them.
                    )
                )
            }
            
            // Fix dependencies if the LLM outputted indices or names instead of our UUIDs.
            // Simplified: If step N depends on step N-1.
            for (i in 1 until stepsList.size) {
                if (stepsList[i].dependencies.isEmpty()) {
                    // Implicit linear dependency
                    stepsList[i] = stepsList[i].copy(dependencies = listOf(stepsList[i-1].id))
                }
            }

        } catch (e: Exception) {
            // Fallback plan
            stepsList.add(PlanStep(
                id = UUID.randomUUID().toString(),
                objective = goal,
                rationale = "Fallback single step plan due to parsing error.",
                definitionOfDone = "Goal is met.",
                requiredTools = emptyList(),
                dependencies = emptyList()
            ))
        }

        return ExecutionPlan(
            id = UUID.randomUUID().toString(),
            originalGoal = goal,
            steps = stepsList
        )
    }
}
