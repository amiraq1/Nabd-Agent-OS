package com.nabd.ai.local.rag.ingestion

import com.nabd.ai.local.rag.models.KnowledgeChunk
import java.util.UUID

/**
 * DocumentChunker: Splits large document text into smaller, overlapping semantic chunks.
 */
class DocumentChunker(
    private val chunkSize: Int = 500, // characters
    private val chunkOverlap: Int = 100
) {
    /**
     * Splits the text into a list of KnowledgeChunks.
     */
    fun chunk(documentId: String, text: String): List<KnowledgeChunk> {
        val chunks = mutableListOf<KnowledgeChunk>()
        if (text.isBlank()) return chunks

        var start = 0
        var index = 0
        
        while (start < text.length) {
            val end = (start + chunkSize).coerceAtMost(text.length)
            val content = text.substring(start, end)
            
            chunks.add(
                KnowledgeChunk(
                    id = UUID.randomUUID().toString(),
                    documentId = documentId,
                    content = content,
                    chunkIndex = index++
                )
            )

            if (end == text.length) break
            start += (chunkSize - chunkOverlap)
        }
        
        return chunks
    }
}
