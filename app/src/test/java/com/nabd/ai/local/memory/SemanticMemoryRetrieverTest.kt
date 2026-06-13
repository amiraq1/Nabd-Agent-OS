package com.nabd.ai.local.memory

import com.nabd.ai.local.embedding.EmbeddingManager
import com.nabd.ai.local.embedding.VectorStore
import com.nabd.ai.local.memory.db.MemoryDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SemanticMemoryRetrieverTest {

    @Test
    fun `retrieve semantic context returns formatted string`() = runTest {
        val memoryDao = mockk<MemoryDao>()
        val vectorStore = mockk<VectorStore>()
        val embeddingManager = mockk<EmbeddingManager>()

        coEvery { embeddingManager.getQueryVector(any()) } returns floatArrayOf(0.1f, 0.2f)
        coEvery { vectorStore.findSimilar(any(), any()) } returns listOf("mem1" to 0.9f)
        
        val memoryEntity = MemoryEntity(
            id = "mem1",
            content = "This is a test memory",
            memoryType = "FACT",
            importance = 8,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { memoryDao.getAllMemories() } returns flowOf(listOf(memoryEntity))

        val retriever = SemanticMemoryRetriever(memoryDao, vectorStore, embeddingManager)
        val result = retriever.retrieveSemanticContext("query")

        assertTrue(result.contains("RELEVANT SEMANTIC MEMORIES"))
        assertTrue(result.contains("This is a test memory"))
    }
}
