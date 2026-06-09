package com.nabd.ai.local.rag.ingestion

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.nabd.ai.local.rag.models.DocumentContent
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfDocumentParser(private val context: Context) : DocumentParser {
    override val supportedMimeTypes = setOf("application/pdf")

    override suspend fun parse(uri: Uri): DocumentContent = withContext(Dispatchers.IO) {
        val title = getFileName(uri) ?: uri.lastPathSegment ?: "Unknown_PDF"
        var parsedText = ""

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            var document: PDDocument? = null
            try {
                document = PDDocument.load(inputStream)
                val pdfStripper = PDFTextStripper()
                // You can chunk it page by page for massive PDFs to prevent OOM
                parsedText = pdfStripper.getText(document)
            } finally {
                document?.close()
            }
        } ?: throw IllegalArgumentException("Cannot open input stream for $uri")

        DocumentContent(
            sourceUri = uri.toString(),
            title = title,
            rawText = parsedText,
            metadata = mapOf("type" to "pdf")
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
