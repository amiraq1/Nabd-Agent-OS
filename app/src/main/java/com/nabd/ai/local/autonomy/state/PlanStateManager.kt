package com.nabd.ai.local.autonomy.state

import com.nabd.ai.local.autonomy.session.AgentSession
import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.autonomy.planning.StepState
import com.nabd.ai.local.autonomy.history.TimelineEvent
import com.nabd.ai.local.autonomy.history.EventType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PlanStateManager(private val stateDir: File) {
    init {
        if (!stateDir.exists()) {
            stateDir.mkdirs()
        }
    }

    fun saveSession(session: AgentSession) {
        val file = File(stateDir, "${session.sessionId}.json")
        val json = JSONObject().apply {
            put("sessionId", session.sessionId)
            put("activeGoal", session.activeGoal)
            put("isPaused", session.isPaused)
            
            session.currentPlan?.let { plan ->
                val planObj = JSONObject().apply {
                    put("id", plan.id)
                    put("originalGoal", plan.originalGoal)
                    put("isCompleted", plan.isCompleted)
                    put("isFailed", plan.isFailed)
                    
                    val stepsArr = JSONArray()
                    for (step in plan.steps) {
                        stepsArr.put(JSONObject().apply {
                            put("id", step.id)
                            put("objective", step.objective)
                            put("rationale", step.rationale)
                            put("definitionOfDone", step.definitionOfDone)
                            put("requiredTools", JSONArray(step.requiredTools))
                            put("dependencies", JSONArray(step.dependencies))
                            put("state", step.state.name)
                            step.observation?.let { put("observation", it) }
                            step.error?.let { put("error", it) }
                        })
                    }
                    put("steps", stepsArr)
                }
                put("currentPlan", planObj)
            }

            val timelineArr = JSONArray()
            for (event in session.timeline) {
                timelineArr.put(JSONObject().apply {
                    put("id", event.id)
                    put("type", event.type.name)
                    put("description", event.description)
                    put("timestamp", event.timestamp)
                })
            }
            put("timeline", timelineArr)
        }
        
        file.writeText(json.toString(2))
    }

    fun loadSession(sessionId: String): AgentSession? {
        val file = File(stateDir, "$sessionId.json")
        if (!file.exists()) return null

        try {
            val json = JSONObject(file.readText())
            
            var plan: ExecutionPlan? = null
            if (json.has("currentPlan")) {
                val planObj = json.getJSONObject("currentPlan")
                val stepsArr = planObj.getJSONArray("steps")
                val stepsList = mutableListOf<PlanStep>()
                
                for (i in 0 until stepsArr.length()) {
                    val stepObj = stepsArr.getJSONObject(i)
                    val reqToolsArr = stepObj.getJSONArray("requiredTools")
                    val reqTools = List(reqToolsArr.length()) { reqToolsArr.getString(it) }
                    val depsArr = stepObj.getJSONArray("dependencies")
                    val deps = List(depsArr.length()) { depsArr.getString(it) }
                    
                    stepsList.add(PlanStep(
                        id = stepObj.getString("id"),
                        objective = stepObj.getString("objective"),
                        rationale = stepObj.getString("rationale"),
                        definitionOfDone = stepObj.getString("definitionOfDone"),
                        requiredTools = reqTools,
                        dependencies = deps,
                        state = StepState.valueOf(stepObj.getString("state")),
                        observation = if (stepObj.has("observation")) stepObj.getString("observation") else null,
                        error = if (stepObj.has("error")) stepObj.getString("error") else null
                    ))
                }
                
                plan = ExecutionPlan(
                    id = planObj.getString("id"),
                    originalGoal = planObj.getString("originalGoal"),
                    steps = stepsList,
                    isCompleted = planObj.getBoolean("isCompleted"),
                    isFailed = planObj.getBoolean("isFailed")
                )
            }

            val timelineArr = json.getJSONArray("timeline")
            val timeline = mutableListOf<TimelineEvent>()
            for (i in 0 until timelineArr.length()) {
                val evObj = timelineArr.getJSONObject(i)
                timeline.add(TimelineEvent(
                    id = evObj.getString("id"),
                    type = EventType.valueOf(evObj.getString("type")),
                    description = evObj.getString("description"),
                    timestamp = evObj.getLong("timestamp")
                ))
            }

            return AgentSession(
                sessionId = json.getString("sessionId"),
                activeGoal = json.getString("activeGoal"),
                currentPlan = plan,
                timeline = timeline,
                isPaused = json.getBoolean("isPaused")
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    fun getLatestSessionId(): String? {
        val files = stateDir.listFiles { _, name -> name.endsWith(".json") }
        if (files.isNullOrEmpty()) return null
        return files.maxByOrNull { it.lastModified() }?.nameWithoutExtension
    }
}
