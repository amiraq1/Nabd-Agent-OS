package com.nabd.ai.local.autonomy.engine

import com.nabd.ai.local.autonomy.validator.PlanStep
import com.nabd.ai.local.autonomy.validator.ExecutionPlan
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ExecutionState { PENDING, RUNNING, COMPLETED, FAILED, SKIPPED, CANCELLED }

data class StepExecutionStatus(
    val stepId: UUID,
    val state: ExecutionState,
    val output: Any? = null,
    val errorMessage: String? = null
)

data class EngineUiState(
    val planId: UUID? = null,
    val globalState: ExecutionState = ExecutionState.PENDING,
    val stepStatuses: Map<UUID, StepExecutionStatus> = emptyMap()
)

class ExecutionEngine(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val _engineState = MutableStateFlow(EngineUiState())
    val engineState: StateFlow<EngineUiState> = _engineState.asStateFlow()

    private var executionJob: Job? = null
    private val isPaused = MutableStateFlow(false)

    fun startExecution(plan: ExecutionPlan, executeStepBlock: suspend (PlanStep) -> Any?) {
        executionJob = scope.launch(dispatcher) {
            _engineState.value = EngineUiState(
                planId = plan.id,
                globalState = ExecutionState.RUNNING,
                stepStatuses = plan.steps.associate { it.id to StepExecutionStatus(it.id, ExecutionState.PENDING) }
            )

            val completedSteps = mutableMapOf<UUID, Any?>()

            for (step in plan.steps) {
                // تفعيل ميزة الإيقاف المؤقت التكتيكي
                while (isPaused.value) { delay(100) }
                ensureActive()

                // التحقق من سلامة التبعيات قبل البدء
                val depFailed = step.dependencies.any { 
                    _engineState.value.stepStatuses[it]?.state == ExecutionState.FAILED 
                }
                if (depFailed) {
                    updateStepState(step.id, ExecutionState.SKIPPED)
                    continue
                }

                updateStepState(step.id, ExecutionState.RUNNING)
                try {
                    val result = executeStepBlock(step)
                    completedSteps[step.id] = result
                    updateStepState(step.id, ExecutionState.COMPLETED, result)
                } catch (e: Exception) {
                    updateStepState(step.id, ExecutionState.FAILED, errorMessage = e.localizedMessage)
                    _engineState.value = _engineState.value.copy(globalState = ExecutionState.FAILED)
                    return@launch // إيقاف التنفيذ لطلب إعادة التخطيط (Replanning)
                }
            }
            _engineState.value = _engineState.value.copy(globalState = ExecutionState.COMPLETED)
        }
    }

    fun pause() { isPaused.value = true }
    fun resume() { isPaused.value = false }
    fun cancel() {
        executionJob?.cancel()
        _engineState.value = _engineState.value.copy(globalState = ExecutionState.CANCELLED)
    }

    private fun updateStepState(stepId: UUID, state: ExecutionState, output: Any? = null, errorMessage: String? = null) {
        val currentStatuses = _engineState.value.stepStatuses.toMutableMap()
        currentStatuses[stepId] = StepExecutionStatus(stepId, state, output, errorMessage)
        _engineState.value = _engineState.value.copy(stepStatuses = currentStatuses)
    }
}
