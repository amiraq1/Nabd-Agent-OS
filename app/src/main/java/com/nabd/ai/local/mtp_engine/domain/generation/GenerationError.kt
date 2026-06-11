package com.nabd.ai.local.mtp_engine.domain.generation

/**
 * GenerationError: Represents various error states during text generation.
 * Part of the MTP Generation Domain.
 */
sealed interface GenerationError {
    data class ModelError(val message: String) : GenerationError
    data class ContextFull(val message: String) : GenerationError
    data class EngineFault(val message: String) : GenerationError
    data class Unknown(val message: String) : GenerationError
}
