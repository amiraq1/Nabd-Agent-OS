package com.nabd.ai.local.intelligence.refactor

import com.nabd.ai.local.workspace.diff.FilePatch

data class RefactoringTransaction(
    val id: String,
    val description: String,
    val affectedFiles: List<String>,
    val patches: List<FilePatch>
)
