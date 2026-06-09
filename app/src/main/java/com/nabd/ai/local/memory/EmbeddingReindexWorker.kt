package com.nabd.ai.local.memory

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nabd.ai.local.embedding.EmbeddingManager
import com.nabd.ai.local.memory.db.MemoryDatabase
import kotlinx.coroutines.flow.first

/**
 * EmbeddingReindexWorker: Ensures all memories have up-to-date embeddings.
 */
class EmbeddingReindexWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val embeddingManager: EmbeddingManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = MemoryDatabase.getDatabase(applicationContext)
        val memories = db.memoryDao().getAllMemories().first()
        val embeddingDao = db.memoryEmbeddingDao()

        for (memory in memories) {
            val existing = embeddingDao.getEmbedding(memory.id)
            if (existing == null) {
                embeddingManager.indexMemory(memory)
            }
        }
        return Result.success()
    }
}
