package com.nabd.ai.local.rag.models

data class KnowledgeDocumentVersion(
    val documentId: String,
    val version: Int,
    val checksum: String,
    val indexedAt: Long = System.currentTimeMillis()
)
