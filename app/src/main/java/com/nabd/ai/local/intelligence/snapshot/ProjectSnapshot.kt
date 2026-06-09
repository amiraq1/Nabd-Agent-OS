package com.nabd.ai.local.intelligence.snapshot

import com.nabd.ai.local.intelligence.memory.ArchitectureSnapshot

data class ProjectSnapshot(
    val timestamp: Long,
    val indexedFileCount: Int,
    val symbolCount: Int,
    val architectureSnapshot: ArchitectureSnapshot?
)
