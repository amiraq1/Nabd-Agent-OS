package com.nabd.ai.local.memory

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * MemoryEntity: Room database model for persistent memories.
 */
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memoryType: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val importance: Int, // 1..10
    val sourceConversationId: String? = null
)
