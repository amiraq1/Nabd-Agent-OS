package com.nabd.ai.local.rag.ingestion

import android.content.Context
import android.net.Uri
import com.nabd.ai.local.embedding.EmbeddingManager
import com.nabd.ai.local.rag.db.KnowledgeDao
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity
import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.db.KnowledgeEmbeddingEntity
import com.nabd.ai.local.rag.models.IngestionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * KnowledgeIngestionManager: Orchestrates the pipeline from file selection to vector indexing.
 */
class KnowledgeIngestionManager(
    private val context: Context,
    private val knowledgeDao: KnowledgeDao,
    private val embeddingManager: EmbeddingManager,
    private val chunker: DocumentChunker = DocumentChunker()
) {
    private val knowledgeDir = File(context.filesDir, "knowledge")

    init {
        if (!knowledgeDir.exists()) knowledgeDir.mkdirs()
    }

    suspend fun ingestDocument(uri: Uri, title: String) = withContext(Dispatchers.IO) {
        val documentId = UUID.randomUUID().toString()
        val targetFile = File(knowledgeDir, "$documentId.txt") // Simplification: Treat all as text

        try {
            // 1. Initial Document Record
            val initialDoc = KnowledgeDocumentEntity(
                id = documentId,
                title = title,
                path = targetFile.absolutePath,
                size = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                status = IngestionStatus.PROCESSING.name
            )
            knowledgeDao.insertDocument(initialDoc)

            // 2. Secure Copy & Text Extraction (Simplification: direct text read)
            val content = context.contentResolver.openInputStream(uri)?.use { 
                it.bufferedReader().readText() 
            } ?: ""
            
            targetFile.writeText(content)
            knowledgeDao.updateDocument(initialDoc.copy(size = targetFile.length()))

            // 3. Chunking
            val chunks = chunker.chunk(documentId, content)
            val chunkEntities = chunks.map { 
                KnowledgeChunkEntity(it.id, it.documentId, it.content, it.pageNumber, it.chunkIndex) 
            }
            knowledgeDao.insertChunks(chunkEntities)

            // 4. Embedding Generation
            val embeddings = chunkEntities.map { chunk ->
                val vector = embeddingManager.getQueryVector(chunk.content)
                KnowledgeEmbeddingEntity(chunk.id, documentId, vector, vector.size)
            }
            knowledgeDao.insertEmbeddings(embeddings)

            // 5. Final Status Update
            knowledgeDao.updateDocument(initialDoc.copy(
                status = IngestionStatus.COMPLETED.name,
                size = targetFile.length(),
                updatedAt = System.currentTimeMillis()
            ))

        } catch (e: Exception) {
            val doc = knowledgeDao.getDocumentById(documentId)
            if (doc != null) {
                knowledgeDao.updateDocument(doc.copy(status = IngestionStatus.FAILED.name))
            }
        }
    }

    suspend fun deleteDocument(documentId: String) = withContext(Dispatchers.IO) {
        val doc = knowledgeDao.getDocumentById(documentId)
        if (doc != null) {
            knowledgeDao.deleteDocument(doc)
            knowledgeDao.deleteChunksByDocumentId(documentId)
            knowledgeDao.deleteEmbeddingsByDocumentId(documentId)
            val file = File(doc.path)
            if (file.exists()) file.delete()
        }
    }
}
