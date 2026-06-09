package com.nabd.ai.local.rag.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: KnowledgeDocumentEntity)

    @Query("SELECT * FROM knowledge_documents")
    fun getAllDocuments(): Flow<List<KnowledgeDocumentEntity>>

    @Query("SELECT * FROM knowledge_documents WHERE id = :id")
    suspend fun getDocumentById(id: String): KnowledgeDocumentEntity?

    @Delete
    suspend fun deleteDocument(document: KnowledgeDocumentEntity)

    @Update
    suspend fun updateDocument(document: KnowledgeDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<KnowledgeChunkEntity>)

    @Query("SELECT * FROM knowledge_chunks WHERE documentId = :documentId")
    suspend fun getChunksForDocument(documentId: String): List<KnowledgeChunkEntity>

    @Query("SELECT * FROM knowledge_chunks WHERE id = :id")
    suspend fun getChunkById(id: String): KnowledgeChunkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbeddings(embeddings: List<KnowledgeEmbeddingEntity>)

    @Query("SELECT * FROM knowledge_embeddings")
    suspend fun getAllEmbeddings(): List<KnowledgeEmbeddingEntity>

    @Query("SELECT * FROM knowledge_chunks")
    suspend fun getAllChunks(): List<KnowledgeChunkEntity>

    @Query("DELETE FROM knowledge_chunks WHERE documentId = :documentId")
    suspend fun deleteChunksByDocumentId(documentId: String)

    @Query("DELETE FROM knowledge_embeddings WHERE documentId = :documentId")
    suspend fun deleteEmbeddingsByDocumentId(documentId: String)
}
