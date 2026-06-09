package com.nabd.ai.local.rag.storage

import com.nabd.ai.local.embedding.Similarity
import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.db.KnowledgeDao
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity
import com.nabd.ai.local.rag.db.KnowledgeEmbeddingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalVectorDatabase(
    private val dao: KnowledgeDao
) : VectorStore {
    override suspend fun upsertDocument(
        document: KnowledgeDocumentEntity,
        chunks: List<KnowledgeChunkEntity>,
        embeddings: List<KnowledgeEmbeddingEntity>
    ) = withContext(Dispatchers.IO) {
        dao.insertDocument(document)
        dao.deleteChunksByDocumentId(document.id)
        dao.deleteEmbeddingsByDocumentId(document.id)
        dao.insertChunks(chunks)
        dao.insertEmbeddings(embeddings)
    }

    override suspend fun similaritySearch(queryEmbedding: FloatArray, topK: Int): List<Pair<KnowledgeChunkEntity, Float>> = withContext(Dispatchers.IO) {
        val allEmbeddings = dao.getAllEmbeddings()
        val scores = allEmbeddings.map { entity ->
            val score = Similarity.cosineSimilarity(queryEmbedding, entity.embedding)
            entity to score
        }.sortedByDescending { it.second }.take(topK)

        scores.mapNotNull { (emb, score) ->
            val chunk = dao.getChunkById(emb.chunkId)
            if (chunk != null) {
                chunk to score
            } else null
        }
    }
    
    override suspend fun getAllChunks(): List<KnowledgeChunkEntity> = withContext(Dispatchers.IO) {
        // Since we don't have getAllChunks in Dao, we can get via document or query individually.
        // Actually, let's implement a workaround or add it to Dao if needed.
        // We will query from embeddings as a proxy for all chunks.
        val embeddings = dao.getAllEmbeddings()
        embeddings.mapNotNull { dao.getChunkById(it.chunkId) }
    }

    override suspend fun getDocumentById(id: String) = dao.getDocumentById(id)
    
    override suspend fun deleteDocument(id: String) = withContext(Dispatchers.IO) {
        dao.getDocumentById(id)?.let { dao.deleteDocument(it) }
        dao.deleteChunksByDocumentId(id)
        dao.deleteEmbeddingsByDocumentId(id)
    }
}
