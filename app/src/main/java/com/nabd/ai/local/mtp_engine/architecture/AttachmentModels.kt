package com.nabd.ai.local.mtp_engine.architecture

data class AttachmentMeta(val items: List<AttachmentItem> = emptyList())

data class AttachmentItem(
    val originalUri: String? = null,
    val type: String,               // "image", "video", "file", "pdf"
    val fileName: String? = null,
    val mimeType: String? = null,
    val imageIndex: Int? = null,
    val pageCount: Int? = null,
    val warning: String? = null,
    val textContent: String? = null,
    val transcription: String? = null
)

/**
 * SelectedAttachment: Represents a file or media selected by the user for a prompt.
 * Migrated from legacy Agora structure for Nabd's tactical architecture.
 */
data class SelectedAttachment(
    val uri: String,
    val type: String,               // "image", "video", "file", "pdf"
    val frameCount: Int? = null,
    val sliceIntervalMs: Long? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val fileSize: Long? = null,
    val processedFrames: List<String>? = null,
    val selectedPages: Set<Int>? = null,
    val preRenderedPaths: List<String>? = null
)
