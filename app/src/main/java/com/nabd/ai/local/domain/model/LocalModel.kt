package com.nabd.ai.local.domain.model

/**
 * LocalModel: Metadata representing a model file stored in internal storage.
 */
data class LocalModel(
    val id: String,
    val fileName: String,
    val path: String,
    val sizeBytes: Long,
    val importedAt: Long
)

/**
 * ModelImportState: Lifecycle states for the model import pipeline.
 */
sealed interface ModelImportState {
    data object Idle : ModelImportState
    data object Validating : ModelImportState
    data class Copying(val progress: Float) : ModelImportState
    data object Completed : ModelImportState
    data class Failed(val error: String) : ModelImportState
}
