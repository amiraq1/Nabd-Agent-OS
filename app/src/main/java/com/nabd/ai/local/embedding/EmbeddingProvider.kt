package com.nabd.ai.local.embedding

import kotlin.math.sqrt

/**
 * EmbeddingProvider: Abstraction for text-to-vector generation.
 */
interface EmbeddingProvider {
    suspend fun embed(text: String): FloatArray
    suspend fun embedBatch(texts: List<String>): List<FloatArray>
    val dimensions: Int
}

/**
 * Utility for semantic similarity calculations.
 */
object Similarity {
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Vectors must have the same dimensions" }
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denominator = sqrt(normA.toDouble()) * sqrt(normB.toDouble())
        return if (denominator == 0.0) 0.0f else (dotProduct / denominator).toFloat()
    }
}
