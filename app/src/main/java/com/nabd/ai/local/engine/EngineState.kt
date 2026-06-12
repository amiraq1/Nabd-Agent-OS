package com.nabd.ai.local.engine

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the current state of the LLM Inference Engine.
 * Follows a strict state machine to ensure memory safety and predictable behavior.
 */
sealed interface EngineState {
    /** Initial state: engine created but not configured. */
    data object Uninitialized : EngineState

    /** Engine is currently being initialized/model loading. */
    data object Initializing : EngineState

    /** Engine successfully initialized and ready for inference. */
    data object Ready : EngineState

    /** Inference is actively running. */
    data object Generating : EngineState

    /** A terminal error state. */
    data class Error(val throwable: Throwable) : EngineState

    /** Model has been removed from memory and resources released. */
    data object Released : EngineState
}

/**
 * Observation interface for tracking engine lifecycle.
 */
interface EngineStatus {
    val state: StateFlow<EngineState>
}
