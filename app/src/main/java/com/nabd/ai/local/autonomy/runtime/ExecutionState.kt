package com.nabd.ai.local.autonomy.runtime

enum class AutonomousExecutionState {
    IDLE,
    PLANNING,
    EXECUTING,
    REFLECTING,
    WAITING_APPROVAL,
    BLOCKED,
    PAUSED,
    COMPLETED,
    FAILED
}
