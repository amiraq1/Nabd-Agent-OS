package com.nabd.ai.local.autonomy.runtime

import com.nabd.ai.local.agent.orchestrator.ToolOrchestrator
import com.nabd.ai.local.autonomy.history.EventType
import com.nabd.ai.local.autonomy.history.ExecutionTimeline
import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.autonomy.planning.StepState
import com.nabd.ai.local.autonomy.planning.TaskPlanner
import com.nabd.ai.local.autonomy.reflection.ReflectionEngine
import com.nabd.ai.local.autonomy.replanning.ReplanningManager
import com.nabd.ai.local.autonomy.resources.ResourceMonitor
import com.nabd.ai.local.autonomy.safety.ExecutionGuardrails
import com.nabd.ai.local.autonomy.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

class AutonomousAgentRunner(
    private val planner: TaskPlanner,
    private val reflectionEngine: ReflectionEngine,
    private val replanningManager: ReplanningManager,
    private val orchestrator: ToolOrchestrator,
    private val sessionManager: SessionManager,
    private val guardrails: ExecutionGuardrails,
    private val timeline: ExecutionTimeline,
    private val resourceMonitor: ResourceMonitor
) {
    private val _state = MutableStateFlow(AutonomousExecutionState.IDLE)
    val state: StateFlow<AutonomousExecutionState> = _state.asStateFlow()

    private var isCancelled = false

    suspend fun startGoal(goal: String) {
        isCancelled = false
        val session = sessionManager.createSession(goal)
        timeline.addEvent(EventType.PLAN_CREATED, "Received goal: ${goal.take(50)}...")
        
        _state.value = AutonomousExecutionState.PLANNING
        val plan = planner.generatePlan(goal, emptyList()) // Pass tools list properly in real usage
        session.currentPlan = plan
        sessionManager.save()
        
        timeline.addEvent(EventType.PLAN_CREATED, "Generated plan with ${plan.steps.size} steps.")
        
        runLoop(plan)
    }

    suspend fun resumeGoal() {
        isCancelled = false
        val session = sessionManager.resumeLatestSession() ?: return
        val plan = session.currentPlan ?: return
        
        if (session.isPaused) {
            session.isPaused = false
            timeline.addEvent(EventType.RESUMED, "Resumed session ${session.sessionId}")
        }
        
        runLoop(plan)
    }

    fun pause() {
        sessionManager.currentSession?.isPaused = true
        _state.value = AutonomousExecutionState.PAUSED
        timeline.addEvent(EventType.PAUSED, "Execution paused by user.")
        sessionManager.save()
    }

    fun cancel() {
        isCancelled = true
        _state.value = AutonomousExecutionState.IDLE
        timeline.addEvent(EventType.CANCELLED, "Execution cancelled by user.")
        sessionManager.save()
    }

    private suspend fun runLoop(plan: ExecutionPlan) {
        while (!plan.isCompleted && !plan.isFailed && !isCancelled) {
            
            if (resourceMonitor.shouldPauseExecution()) {
                pause()
                return
            }

            if (sessionManager.currentSession?.isPaused == true) {
                _state.value = AutonomousExecutionState.PAUSED
                return
            }

            val guardrailError = guardrails.checkGuardrails()
            if (guardrailError != null) {
                plan.isFailed = true
                timeline.addEvent(EventType.STEP_FAILED, guardrailError)
                _state.value = AutonomousExecutionState.FAILED
                sessionManager.save()
                return
            }

            val nextStep = plan.getNextExecutableStep()
            if (nextStep == null) {
                // If not completed but no next step, we are blocked by failed dependencies
                plan.isFailed = true
                _state.value = AutonomousExecutionState.BLOCKED
                timeline.addEvent(EventType.STEP_FAILED, "Execution blocked. No executable steps remaining.")
                sessionManager.save()
                return
            }

            _state.value = AutonomousExecutionState.EXECUTING
            timeline.addEvent(EventType.STEP_STARTED, "Starting step: ${nextStep.objective}")
            plan.updateStepState(nextStep.id, StepState.IN_PROGRESS)
            sessionManager.save()

            // Map the step objective into a prompt for the ToolOrchestrator
            val prompt = "Objective: ${nextStep.objective}\nDefinition of Done: ${nextStep.definitionOfDone}\nExecute this step using the available tools."
            
            // Execute the Tool Orchestrator loop (which itself is a ReAct loop bounded by maxIterations)
            orchestrator.runLoop(prompt)
            
            // Collect the final trace from the orchestrator to pass to reflection
            val orchestratorTrace = orchestrator.trace.value.entries.joinToString("\n") { "${it.type}: ${it.content}" }
            
            _state.value = AutonomousExecutionState.REFLECTING
            timeline.addEvent(EventType.REFLECTION_EVALUATED, "Evaluating step results...")
            
            val eval = reflectionEngine.evaluateStep(nextStep, orchestratorTrace)
            
            if (eval.isSuccess) {
                guardrails.recordSuccess()
                plan.updateStepState(nextStep.id, StepState.COMPLETED, observation = orchestratorTrace)
                timeline.addEvent(EventType.STEP_COMPLETED, "Step completed successfully.")
            } else {
                guardrails.recordFailure()
                plan.updateStepState(nextStep.id, StepState.FAILED, error = eval.reasoning)
                timeline.addEvent(EventType.STEP_FAILED, "Step failed: ${eval.reasoning}")
                
                if (eval.suggestedCorrections.isNotEmpty()) {
                    guardrails.recordReplan()
                    timeline.addEvent(EventType.REPLAN_TRIGGERED, "Replanning to apply corrections...")
                    val updatedPlan = replanningManager.insertCorrections(plan, nextStep.id, eval.suggestedCorrections)
                    sessionManager.currentSession?.currentPlan = updatedPlan
                }
            }

            guardrails.recordStep()
            sessionManager.save()
            delay(1000) // Brief pause to not spin wildly
        }

        if (plan.isCompleted) {
            _state.value = AutonomousExecutionState.COMPLETED
            timeline.addEvent(EventType.PLAN_COMPLETED, "Goal completed successfully.")
        } else if (plan.isFailed) {
            _state.value = AutonomousExecutionState.FAILED
            timeline.addEvent(EventType.PLAN_FAILED, "Goal failed.")
        }
        
        sessionManager.save()
    }
}
