package com.nabd.ai.local.rag.storage

import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity
import com.nabd.ai.local.rag.db.KnowledgeEmbeddingEntity

interface VectorStore {
    suspend fun upsertDocument(
        document: KnowledgeDocumentEntity,
        chunks: List<KnowledgeChunkEntity>,
        embeddings: List<KnowledgeEmbeddingEntity>
    )

    suspend fun similaritySearch(queryEmbedding: FloatArray, topK: Int = 100): List<Pair<KnowledgeChunkEntity, Float>>
    
    suspend fun getAllChunks(): List<KnowledgeChunkEntity>
    
    suspend fun getDocumentById(id: String): KnowledgeDocumentEntity?
    
    suspend fun deleteDocument(id: String)
}
