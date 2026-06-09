package com.nabd.ai.local.rag.ingestion

import android.net.Uri
import com.nabd.ai.local.rag.models.DocumentContent
import kotlinx.coroutines.flow.Flow

interface DocumentParser {
    val supportedMimeTypes: Set<String>
    
    /**
     * Parses the document in a streaming/suspending fashion and returns chunks of text if needed,
     * or a complete DocumentContent if it fits reasonably. For 50MB files, 
     * a real streaming parser might yield Flow<DocumentContentChunk>, but to fit the interface,
     * we will at least ensure parsing is off the main thread.
     */
    suspend fun parse(uri: Uri): DocumentContent
}
