package com.nabd.ai.local.embedding.db

import androidx.room.*

@Dao
interface MemoryEmbeddingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbedding(embedding: MemoryEmbeddingEntity)

    @Query("SELECT * FROM memory_embeddings WHERE memoryId = :memoryId")
    suspend fun getEmbedding(memoryId: String): MemoryEmbeddingEntity?

    @Query("SELECT * FROM memory_embeddings")
    suspend fun getAllEmbeddings(): List<MemoryEmbeddingEntity>

    @Query("DELETE FROM memory_embeddings WHERE memoryId = :memoryId")
    suspend fun deleteEmbedding(memoryId: String)
}
