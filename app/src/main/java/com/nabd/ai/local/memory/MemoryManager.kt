package com.nabd.ai.local.memory

import com.nabd.ai.local.embedding.EmbeddingManager
import com.nabd.ai.local.memory.db.MemoryDao
import kotlinx.coroutines.flow.Flow

/**
 * MemoryManager: Central orchestrator for all memory operations.
 * High-level API for storing, retrieving, and managing memories.
 */
class MemoryManager(
    private val memoryDao: MemoryDao,
    private val embeddingManager: EmbeddingManager? = null
) {

    fun getAllMemories(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun storeMemory(
        type: MemoryType,
        content: String,
        importance: Int = 5,
        conversationId: String? = null
    ) {
        val entity = MemoryEntity(
            memoryType = type.name,
            content = content,
            importance = importance.coerceIn(1, 10),
            sourceConversationId = conversationId
        )
        memoryDao.insertMemory(entity)
        embeddingManager?.indexMemory(entity)
    }

    suspend fun searchMemories(query: String): List<MemoryEntity> {
        return memoryDao.searchMemories(query)
    }

    suspend fun getRelevantContext(limit: Int = 5): String {
        val memories = memoryDao.getTopImportantMemories(limit)
        return memories.joinToString("\n") { "[${it.memoryType}] ${it.content}" }
    }

    suspend fun deleteMemory(id: String) {
        memoryDao.deleteById(id)
        embeddingManager?.removeMemory(id)
    }
}
