package com.nabd.ai.local.workspace.diff

data class FilePatch(
    val relativePath: String,
    val unifiedDiff: String
)
