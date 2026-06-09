package com.nabd.ai.local.embedding

import com.nabd.ai.local.embedding.db.MemoryEmbeddingDao
import com.nabd.ai.local.embedding.db.MemoryEmbeddingEntity

/**
 * VectorStore: Manages the storage and retrieval of vector embeddings.
 */
class VectorStore(private val embeddingDao: MemoryEmbeddingDao) {

    suspend fun storeEmbedding(memoryId: String, embedding: FloatArray) {
        embeddingDao.insertEmbedding(
            MemoryEmbeddingEntity(
                memoryId = memoryId,
                embedding = embedding,
                dimensions = embedding.size
            )
        )
    }

    suspend fun getEmbedding(memoryId: String): FloatArray? {
        return embeddingDao.getEmbedding(memoryId)?.embedding
    }

    suspend fun getAllEmbeddings(): List<MemoryEmbeddingEntity> {
        return embeddingDao.getAllEmbeddings()
    }

    suspend fun deleteEmbedding(memoryId: String) {
        embeddingDao.deleteEmbedding(memoryId)
    }

    /**
     * Finds memories similar to the query embedding.
     * @return List of memoryIds with their similarity scores.
     */
    suspend fun findSimilar(query: FloatArray, topK: Int = 10): List<Pair<String, Float>> {
        val all = embeddingDao.getAllEmbeddings()
        return all.map { 
            it.memoryId to Similarity.cosineSimilarity(query, it.embedding)
        }
        .sortedByDescending { it.second }
        .take(topK)
    }
}
