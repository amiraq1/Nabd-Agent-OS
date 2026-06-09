package com.nabd.ai.local.embedding

import com.nabd.ai.local.memory.MemoryEntity

/**
 * EmbeddingManager: Coordinates embedding generation and storage.
 */
class EmbeddingManager(
    private val provider: EmbeddingProvider,
    private val vectorStore: VectorStore
) {
    /**
     * Generates and stores an embedding for the given memory.
     */
    suspend fun indexMemory(memory: MemoryEntity) {
        val vector = provider.embed(memory.content)
        vectorStore.storeEmbedding(memory.id, vector)
    }

    /**
     * Deletes the embedding for the given memory ID.
     */
    suspend fun removeMemory(memoryId: String) {
        vectorStore.deleteEmbedding(memoryId)
    }

    /**
     * Generates an embedding for a query.
     */
    suspend fun getQueryVector(query: String): FloatArray {
        return provider.embed(query)
    }
}
