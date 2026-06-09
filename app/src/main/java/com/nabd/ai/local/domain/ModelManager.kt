package com.nabd.ai.local.domain

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.nabd.ai.local.domain.model.LocalModel
import com.nabd.ai.local.domain.model.ModelImportState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * ModelManager: Handles model ingestion, discovery, and file operations.
 */
class ModelManager(private val context: Context) {

    private val modelsDir = File(context.filesDir, "models")
    private val _importState = MutableStateFlow<ModelImportState>(ModelImportState.Idle)
    val importState: Flow<ModelImportState> = _importState

    init {
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
    }

    /**
     * Imports a model from a SAF Uri into internal storage.
     * Uses chunked streaming to handle multi-GB files safely.
     */
    suspend fun importModel(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            _importState.value = ModelImportState.Validating
            
            val fileName = getFileName(uri) ?: "unknown_model.gguf"
            if (!fileName.endsWith(".gguf")) {
                _importState.value = ModelImportState.Failed("Only .gguf models are supported.")
                return@withContext
            }

            val targetFile = File(modelsDir, fileName)
            val totalSize = getFileSize(uri)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(1024 * 1024) // 1MB chunks
                    var bytesRead: Int
                    var totalBytesCopied = 0L

                    _importState.value = ModelImportState.Copying(0f)

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesCopied += bytesRead
                        
                        if (totalSize > 0) {
                            val progress = totalBytesCopied.toFloat() / totalSize.toFloat()
                            _importState.value = ModelImportState.Copying(progress)
                        }
                    }
                }
            }
            
            _importState.value = ModelImportState.Completed
        } catch (e: Exception) {
            _importState.value = ModelImportState.Failed(e.message ?: "Unknown import error")
        }
    }

    fun scanModels(): Flow<List<LocalModel>> = flow {
        val files = modelsDir.listFiles()?.filter { it.extension == "gguf" } ?: emptyList()
        val models = files.map { file ->
            LocalModel(
                id = UUID.randomUUID().toString(),
                fileName = file.name,
                path = file.absolutePath,
                sizeBytes = file.length(),
                importedAt = file.lastModified()
            )
        }
        emit(models)
    }.flowOn(Dispatchers.IO)

    suspend fun deleteModel(model: LocalModel) = withContext(Dispatchers.IO) {
        val file = File(model.path)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun getFileSize(uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }
}
