package com.nabd.ai.local.rag.models

import java.util.UUID

/**
 * KnowledgeDocument: Metadata for an ingested document.
 */
data class KnowledgeDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val path: String,
    val size: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: IngestionStatus = IngestionStatus.PENDING
)

enum class IngestionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

/**
 * KnowledgeChunk: A granular piece of a document for semantic retrieval.
 */
data class KnowledgeChunk(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val content: String,
    val pageNumber: Int? = null,
    val chunkIndex: Int
)
