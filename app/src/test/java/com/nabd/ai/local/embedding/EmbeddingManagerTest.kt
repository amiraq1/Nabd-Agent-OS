package com.nabd.ai.local.embedding

import com.nabd.ai.local.embedding.db.MemoryEmbeddingDao
import com.nabd.ai.local.embedding.db.MemoryEmbeddingEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EmbeddingManagerTest {

    @Test
    fun `getQueryVector returns valid vector`() = runTest {
        val provider = mockk<EmbeddingProvider>()
        val dao = mockk<MemoryEmbeddingDao>()
        val store = VectorStore(dao)
        
        coEvery { provider.embed("test") } returns floatArrayOf(1.0f, 0.0f)
        
        val manager = EmbeddingManager(provider, store)
        val vector = manager.getQueryVector("test")
        
        assertArrayEquals(floatArrayOf(1.0f, 0.0f), vector)
    }
}
