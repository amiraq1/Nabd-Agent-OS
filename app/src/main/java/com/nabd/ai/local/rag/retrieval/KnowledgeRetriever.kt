package com.nabd.ai.local.rag.retrieval

import com.nabd.ai.local.rag.storage.LocalVectorDatabase
import com.nabd.ai.local.rag.db.KnowledgeDao

/**
 * KnowledgeRetriever: Performs semantic search over ingested documents with source attribution using Hybrid Retrieval.
 */
class KnowledgeRetriever(
    private val hybridRetriever: HybridRetriever,
    private val knowledgeDao: KnowledgeDao
) {
    /**
     * Retrieves relevant document passages for a query.
     */
    suspend fun retrieveKnowledge(query: String, topK: Int = 5): String {
        val chunks = hybridRetriever.retrieve(query, topK)
        
        if (chunks.isEmpty()) return ""

        val results = mutableListOf<String>()
        for (chunk in chunks) {
            val doc = knowledgeDao.getDocumentById(chunk.documentId) ?: continue
            val citation = CitationFormatter.formatCitation(chunk, doc)
            
            results.add("""
                $citation
                Content: ${chunk.content}
                ---
            """.trimIndent())
        }

        return if (results.isNotEmpty()) {
            "--- RELEVANT KNOWLEDGE BASE ---\n" +
            results.joinToString("\n") +
            "\n-------------------------------"
        } else {
            ""
        }
    }
}
