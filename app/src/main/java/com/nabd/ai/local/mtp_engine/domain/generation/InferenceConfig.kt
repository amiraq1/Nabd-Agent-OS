package com.nabd.ai.local.mtp_engine.domain.generation

import androidx.compose.runtime.Stable

/**
 * InferenceConfig: Tactical Sampling Configuration for local LLM inference.
 * Optimized for sharp, accurate, and direct responses.
 */
@Stable
data class InferenceConfig(
    val temperature: Float = 0.4f,       // Low heat: Sharp, accurate, and direct answers
    val topP: Float = 0.9f,              // Nucleus sampling
    val minP: Float = 0.05f,             // Exclude improbable paths (crucial llama.cpp feature)
    val maxTokens: Int = 2048,
    val repetitionPenalty: Float = 1.15f, // Prevent stereotypical repetition
    val systemPrompt: String = NabdPersona.NUCLEAR_PROMPT
)
