package com.nabd.ai.local.rag.retrieval

import com.nabd.ai.local.embedding.EmbeddingManager
import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.storage.LocalVectorDatabase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HybridRetrieverTest {

    @Test
    fun `hybrid retrieval combines vector and BM25 scores correctly`() = runTest {
        val db = mockk<LocalVectorDatabase>()
        val embeddingManager = mockk<EmbeddingManager>()

        val query = "kotlin coroutines"
        coEvery { embeddingManager.getQueryVector(query) } returns floatArrayOf(0.1f)
        
        val chunk1 = KnowledgeChunkEntity("1", "doc", "Kotlin coroutines are great", null, 0)
        val chunk2 = KnowledgeChunkEntity("2", "doc", "Java threads are okay", null, 1)

        coEvery { db.similaritySearch(any(), any()) } returns listOf(
            chunk1 to 0.9f, // High vector match
            chunk2 to 0.8f  // Moderate vector match
        )

        val retriever = HybridRetriever(db, embeddingManager)
        val results = retriever.retrieve(query, 2)
        
        assertEquals(2, results.size)
        assertEquals("1", results[0].id) // Chunk 1 should win because of BM25 + Vector
    }
}
