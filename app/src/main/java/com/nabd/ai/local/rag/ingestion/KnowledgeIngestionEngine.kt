package com.nabd.ai.local.rag.ingestion

import android.content.Context
import android.net.Uri
import com.nabd.ai.local.embedding.EmbeddingProvider
import com.nabd.ai.local.rag.chunking.TextSplitter
import com.nabd.ai.local.rag.db.KnowledgeChunkEntity
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity
import com.nabd.ai.local.rag.db.KnowledgeEmbeddingEntity
import com.nabd.ai.local.rag.storage.LocalVectorDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID

enum class IngestionState {
    PARSING, CHUNKING, EMBEDDING, INDEXING, COMPLETED, FAILED
}

class KnowledgeIngestionEngine(
    private val context: Context,
    private val textSplitter: TextSplitter,
    private val embeddingProvider: EmbeddingProvider,
    private val vectorDatabase: LocalVectorDatabase
) {
    fun ingest(uri: Uri): Flow<IngestionState> = flow {
        try {
            emit(IngestionState.PARSING)
            val parser = getParserForUri(uri)
            val documentContent = parser.parse(uri)

            emit(IngestionState.CHUNKING)
            val chunksText = textSplitter.splitText(documentContent.rawText)

            val documentId = UUID.randomUUID().toString()
            val documentEntity = KnowledgeDocumentEntity(
                id = documentId,
                title = documentContent.title,
                path = documentContent.sourceUri,
                size = documentContent.rawText.length.toLong(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                status = "COMPLETED"
            )

            val chunkEntities = chunksText.mapIndexed { index, content ->
                KnowledgeChunkEntity(
                    id = UUID.randomUUID().toString(),
                    documentId = documentId,
                    content = content,
                    pageNumber = null, 
                    chunkIndex = index
                )
            }

            emit(IngestionState.EMBEDDING)
            val embeddings = chunkEntities.map { chunk ->
                val vector = embeddingProvider.embed(chunk.content)
                KnowledgeEmbeddingEntity(
                    chunkId = chunk.id,
                    documentId = documentId,
                    embedding = vector,
                    dimensions = vector.size
                )
            }

            emit(IngestionState.INDEXING)
            vectorDatabase.upsertDocument(documentEntity, chunkEntities, embeddings)

            emit(IngestionState.COMPLETED)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(IngestionState.FAILED)
        }
    }.flowOn(Dispatchers.IO)

    private fun getParserForUri(uri: Uri): DocumentParser {
        val mimeType = context.contentResolver.getType(uri)
        return if (mimeType == "application/pdf") {
            PdfDocumentParser(context)
        } else {
            TextDocumentParser(context)
        }
    }
}
