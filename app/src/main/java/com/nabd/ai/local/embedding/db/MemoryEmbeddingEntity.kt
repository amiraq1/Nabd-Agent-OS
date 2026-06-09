package com.nabd.ai.local.embedding.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MemoryEmbeddingEntity: Stores vector embeddings for memories in Room.
 */
@Entity(tableName = "memory_embeddings")
data class MemoryEmbeddingEntity(
    @PrimaryKey val memoryId: String,
    val embedding: FloatArray, // Room doesn't support FloatArray directly, needs converter
    val dimensions: Int,
    val createdAt: Long = System.currentTimeMillis()
)
