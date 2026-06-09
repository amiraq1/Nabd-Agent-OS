package com.nabd.ai.local.rag.models

data class DocumentContent(
    val sourceUri: String,
    val title: String,
    val rawText: String,
    val metadata: Map<String, String> = emptyMap()
)
