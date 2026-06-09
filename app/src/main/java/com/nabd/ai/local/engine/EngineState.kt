package com.nabd.ai.local.engine

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the current state of the LLM Inference Engine.
 * Follows a strict state machine to ensure memory safety and predictable behavior.
 */
sealed interface EngineState {
    /** Initial state: engine created but no model loaded. */
    data object Initialized : EngineState

    /** Model is currently being loaded into memory. */
    data object Loading : EngineState

    /** Model successfully loaded and ready for inference. */
    data object Loaded : EngineState

    /** Inference is actively running. */
    data object Generating : EngineState

    /** Signal sent to stop generation, cleanup in progress. */
    data object Stopping : EngineState

    /** Model has been removed from memory. */
    data object Unloaded : EngineState

    /** A terminal error state. */
    data class Failed(val error: String) : EngineState
}

/**
 * Observation interface for tracking engine lifecycle.
 */
interface EngineStatus {
    val state: StateFlow<EngineState>
}
