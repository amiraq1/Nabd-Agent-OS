package com.nabd.ai.local.memory

import com.nabd.ai.local.embedding.EmbeddingManager
import com.nabd.ai.local.embedding.VectorStore
import com.nabd.ai.local.memory.db.MemoryDao
import kotlinx.coroutines.flow.first

/**
 * SemanticMemoryRetriever: Implements meaning-aware retrieval with hybrid ranking.
 */
class SemanticMemoryRetriever(
    private val memoryDao: MemoryDao,
    private val vectorStore: VectorStore,
    private val embeddingManager: EmbeddingManager
) {
    /**
     * Retrieves relevant memories based on semantic similarity, importance, and recency.
     */
    suspend fun retrieveSemanticContext(query: String, topK: Int = 5): String {
        val queryVector = embeddingManager.getQueryVector(query)
        val similarMemories = vectorStore.findSimilar(queryVector, topK = topK * 2)

        if (similarMemories.isEmpty()) return ""

        // Fetch actual memory entities
        val memories = memoryDao.getAllMemories().first() // Simplified fetch
        val memoryMap = memories.associateBy { it.id }

        // Hybrid Ranking Formula: Score = Similarity * 0.7 + (Importance / 10) * 0.2 + (Recency) * 0.1
        val rankedMemories = similarMemories.mapNotNull { (id, similarity) ->
            val memory = memoryMap[id] ?: return@mapNotNull null
            val recencyScore = calculateRecencyScore(memory.updatedAt)
            val finalScore = (similarity * 0.7f) + (memory.importance / 10f * 0.2f) + (recencyScore * 0.1f)
            memory to finalScore
        }
        .sortedByDescending { it.second }
        .take(topK)
        .map { it.first }

        return if (rankedMemories.isNotEmpty()) {
            "--- RELEVANT SEMANTIC MEMORIES ---\n" +
            rankedMemories.joinToString("\n") { "[${it.memoryType}] ${it.content}" } +
            "\n----------------------------------"
        } else {
            ""
        }
    }

    private fun calculateRecencyScore(timestamp: Long): Float {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val dayInMillis = 24 * 60 * 60 * 1000L
        return when {
            diff < dayInMillis -> 1.0f
            diff < 7 * dayInMillis -> 0.7f
            diff < 30 * dayInMillis -> 0.4f
            else -> 0.1f
        }
    }
}
