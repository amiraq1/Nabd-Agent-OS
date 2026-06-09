package com.nabd.ai.local.rag.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_documents")
data class KnowledgeDocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val path: String,
    val size: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val status: String
)

@Entity(
    tableName = "knowledge_chunks",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeDocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class KnowledgeChunkEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val content: String,
    val pageNumber: Int?,
    val chunkIndex: Int
)

@Entity(tableName = "knowledge_embeddings")
data class KnowledgeEmbeddingEntity(
    @PrimaryKey val chunkId: String,
    val documentId: String,
    val embedding: FloatArray,
    val dimensions: Int
)
