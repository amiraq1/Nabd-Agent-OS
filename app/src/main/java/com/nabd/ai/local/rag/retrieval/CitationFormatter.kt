package com.nabd.ai.local.rag.retrieval

import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity

object CitationFormatter {
    fun formatCitation(chunk: KnowledgeChunkEntity, document: KnowledgeDocumentEntity): String {
        val pageInfo = if (chunk.pageNumber != null) ", Page ${chunk.pageNumber}" else ""
        return "[Source: ${document.title}$pageInfo]"
    }
}
