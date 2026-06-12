package com.nabd.ai.local.autonomy.engine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nabd.ai.local.autonomy.validator.ExecutionPlan
import java.util.UUID

/**
 * AgentDashboardViewModel: Manages the UI state and persistence for the autonomous agent dashboard.
 */
class AgentDashboardViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val executionEngine: ExecutionEngine
) : ViewModel() {

    companion object {
        private const val KEY_PLAN_ID = "saved_plan_id"
    }

    /**
     * Persists the current plan ID to the SavedStateHandle.
     */
    fun persistPlanState(planId: UUID) {
        savedStateHandle[KEY_PLAN_ID] = planId.toString()
    }

    /**
     * Checks for a saved plan ID and attempts to recover the execution state.
     */
    fun checkAndRecoverState() {
        val savedPlanIdStr: String? = savedStateHandle[KEY_PLAN_ID]
        savedPlanIdStr?.let {
            val restoredPlanId = UUID.fromString(it)
            // TODO: Retrieve the plan from the database and re-inject it into the executionEngine.
        }
    }
}
