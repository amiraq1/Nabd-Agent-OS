package com.nabd.ai.local.autonomy.runtime

import com.nabd.ai.local.autonomy.coordination.MultiAgentCoordinator
import com.nabd.ai.local.autonomy.coordination.AgentRole
import com.nabd.ai.local.autonomy.coordination.AgentMessage
import com.nabd.ai.local.autonomy.history.EventType
import com.nabd.ai.local.autonomy.history.ExecutionTimeline
import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.autonomy.planning.StepState
import com.nabd.ai.local.autonomy.replanning.ReplanningManager
import com.nabd.ai.local.autonomy.resources.ResourceMonitor
import com.nabd.ai.local.autonomy.safety.ExecutionGuardrails
import com.nabd.ai.local.autonomy.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

class AutonomousAgentRunner(
    private val coordinator: MultiAgentCoordinator,
    private val replanningManager: ReplanningManager,
    private val sessionManager: SessionManager,
    private val guardrails: ExecutionGuardrails,
    private val timeline: ExecutionTimeline,
    private val resourceMonitor: ResourceMonitor
) {
    private val _state = MutableStateFlow(AutonomousExecutionState.IDLE)
    val state: StateFlow<AutonomousExecutionState> = _state.asStateFlow()

    private val _currentPlan = MutableStateFlow<ExecutionPlan?>(null)
    val currentPlan: StateFlow<ExecutionPlan?> = _currentPlan.asStateFlow()

    private var isCancelled = false

    init {
        // Initialize current plan if there is an active session
        _currentPlan.value = sessionManager.currentSession?.currentPlan
    }

    suspend fun startGoal(goal: String) {
        isCancelled = false
        val session = sessionManager.createSession(goal)
        _currentPlan.value = null // Reset for new goal
        timeline.addEvent(EventType.PLAN_CREATED, "Received goal: ${goal.take(50)}...")
        
        _state.value = AutonomousExecutionState.PLANNING
        
        // Delegate planning to PlannerAgent via Coordinator
        val planningResponse = coordinator.routeTask(AgentRole.PLANNER, goal)
        val plan = planningResponse.metadata["plan"] as ExecutionPlan
        
        session.currentPlan = plan
        _currentPlan.value = plan
        sessionManager.save()
        
        timeline.addEvent(EventType.PLAN_CREATED, "Generated plan via MultiAgentCoordinator.")
        
        runLoop(plan)
    }

    suspend fun resumeGoal() {
        isCancelled = false
        val session = sessionManager.resumeLatestSession() ?: return
        val plan = session.currentPlan ?: return
        _currentPlan.value = plan
        
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

            // 1. Execute step via ExecutorAgent
            val executorResponse = coordinator.routeTask(
                AgentRole.EXECUTOR,
                "Objective: ${nextStep.objective}\nDefinition of Done: ${nextStep.definitionOfDone}"
            )
            val orchestratorTrace = executorResponse.content
            
            // 2. Review results via ReviewerAgent
            _state.value = AutonomousExecutionState.REFLECTING
            timeline.addEvent(EventType.REFLECTION_EVALUATED, "Evaluating step results...")
            
            val reviewerResponse = coordinator.routeTask(
                AgentRole.REVIEWER,
                orchestratorTrace
            )
            
            val eval = reviewerResponse.metadata["eval"] as? com.nabd.ai.local.autonomy.reflection.EvaluationResult
            
            if (eval?.isSuccess == true) {
                guardrails.recordSuccess()
                plan.updateStepState(nextStep.id, StepState.COMPLETED, observation = orchestratorTrace)
                timeline.addEvent(EventType.STEP_COMPLETED, "Step completed successfully.")
            } else {
                guardrails.recordFailure()
                plan.updateStepState(nextStep.id, StepState.FAILED, error = eval?.reasoning ?: "Unknown failure")
                timeline.addEvent(EventType.STEP_FAILED, "Step failed: ${eval?.reasoning}")
                
                if (eval?.suggestedCorrections?.isNotEmpty() == true) {
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
