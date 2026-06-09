package com.nabd.ai.local.rag.ingestion

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.nabd.ai.local.rag.models.DocumentContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class TextDocumentParser(private val context: Context) : DocumentParser {
    override val supportedMimeTypes = setOf("text/plain", "text/markdown", "application/octet-stream")

    override suspend fun parse(uri: Uri): DocumentContent = withContext(Dispatchers.IO) {
        val title = getFileName(uri) ?: uri.lastPathSegment ?: "Unknown_Document"
        val stringBuilder = StringBuilder()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                var line: String?
                // Basic streaming into memory. For true >50MB txt we might need to yield chunks.
                // Assuming TXT/MD files are mostly manageable.
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }
            }
        } ?: throw IllegalArgumentException("Cannot open input stream for $uri")

        DocumentContent(
            sourceUri = uri.toString(),
            title = title,
            rawText = stringBuilder.toString(),
            metadata = mapOf("type" to "text")
        )
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        return result
    }
}
