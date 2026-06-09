package com.nabd.ai.local.rag.retrieval

import com.nabd.ai.local.embedding.EmbeddingManager
import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.storage.LocalVectorDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HybridRetriever(
    private val vectorDatabase: LocalVectorDatabase,
    private val embeddingManager: EmbeddingManager
) {
    suspend fun retrieve(query: String, topK: Int = 5): List<KnowledgeChunkEntity> = withContext(Dispatchers.IO) {
        val queryVector = embeddingManager.getQueryVector(query)
        
        // 1. Vector Search (topK * 2 for hybrid reranking)
        val vectorResults = vectorDatabase.similaritySearch(queryVector, topK = topK * 2)
        if (vectorResults.isEmpty()) return@withContext emptyList()

        // 2. BM25 / Keyword Score Calculation
        val queryTerms = query.lowercase().split("\\W+".toRegex()).filter { it.length > 2 }
        
        val rankedChunks = vectorResults.map { (chunk, vectorScore) ->
            val contentLower = chunk.content.lowercase()
            
            // Pseudo-BM25: Term Frequency
            var tfScore = 0.0f
            if (queryTerms.isNotEmpty()) {
                val matches = queryTerms.count { contentLower.contains(it) }
                tfScore = matches.toFloat() / queryTerms.size
            }

            // 3. Recency Weight
            val recencyScore = 1.0f

            // Final Formula
            val finalScore = (0.70f * vectorScore) + (0.20f * tfScore) + (0.10f * recencyScore)
            
            chunk to finalScore
        }

        rankedChunks.sortedByDescending { it.second }.take(topK).map { it.first }
    }
}
