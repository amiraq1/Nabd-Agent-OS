package com.nabd.ai.local.rag.ingestion

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nabd.ai.local.embedding.EmbeddingProvider
import com.nabd.ai.local.rag.chunking.TextSplitter
import com.nabd.ai.local.rag.storage.LocalVectorDatabase

class KnowledgeIndexWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val textSplitter: TextSplitter,
    private val embeddingProvider: EmbeddingProvider,
    private val vectorDatabase: LocalVectorDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uriString = inputData.getString("DOCUMENT_URI") ?: return Result.failure()
        val uri = Uri.parse(uriString)

        val engine = KnowledgeIngestionEngine(applicationContext, textSplitter, embeddingProvider, vectorDatabase)
        
        var isSuccess = true
        engine.ingest(uri).collect { state ->
            if (state == IngestionState.FAILED) {
                isSuccess = false
            }
        }

        return if (isSuccess) Result.success() else Result.retry()
    }
}
