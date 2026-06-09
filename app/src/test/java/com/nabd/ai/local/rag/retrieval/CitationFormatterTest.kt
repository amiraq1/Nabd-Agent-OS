package com.nabd.ai.local.rag.retrieval

import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CitationFormatterTest {

    @Test
    fun `formats citation with page number`() {
        val doc = KnowledgeDocumentEntity("doc1", "Android Guide", "path", 100L, 0L, 0L, "COMPLETED")
        val chunk = KnowledgeChunkEntity("chunk1", "doc1", "text", 12, 0)
        
        val citation = CitationFormatter.formatCitation(chunk, doc)
        assertEquals("[Source: Android Guide, Page 12]", citation)
    }

    @Test
    fun `formats citation without page number`() {
        val doc = KnowledgeDocumentEntity("doc1", "Android Guide", "path", 100L, 0L, 0L, "COMPLETED")
        val chunk = KnowledgeChunkEntity("chunk1", "doc1", "text", null, 0)
        
        val citation = CitationFormatter.formatCitation(chunk, doc)
        assertEquals("[Source: Android Guide]", citation)
    }
}
