package com.nabd.ai.local.ui.autonomy

import com.nabd.ai.local.autonomy.coordination.AgentRole
import com.nabd.ai.local.autonomy.runtime.AutonomousExecutionState
import java.util.UUID

/**
 * DashboardUiState: Represents the current state of the entire agent system for the UI.
 */
data class DashboardUiState(
    val activeObjective: String? = null,
    val globalState: AutonomousExecutionState = AutonomousExecutionState.IDLE,
    val agents: List<AgentStatus> = emptyList(),
    val totalMemoryUsage: Long = 0,
    val recentEvents: List<String> = emptyList(),
    val performanceMetrics: PerformanceSummary = PerformanceSummary()
)

data class AgentStatus(
    val id: String,
    val role: AgentRole,
    val isBusy: Boolean,
    val currentTask: String? = null
)

data class PerformanceSummary(
    val successRate: Float = 0f,
    val avgLatencyMs: Long = 0,
    val replanningCount: Int = 0
)

// Placeholder for the actual Composable
// @Composable
// fun AgentDashboard(state: DashboardUiState) { ... }
