package com.nabd.ai.local.intelligence.snapshot

import java.io.File
import org.json.JSONObject

class ProjectSnapshotManager(private val snapshotFile: File) {
    fun saveSnapshot(snapshot: ProjectSnapshot) {
        if (!snapshotFile.parentFile.exists()) {
            snapshotFile.parentFile.mkdirs()
        }
        val json = JSONObject().apply {
            put("timestamp", snapshot.timestamp)
            put("indexedFileCount", snapshot.indexedFileCount)
            put("symbolCount", snapshot.symbolCount)
            // Save architecture snapshot if needed, or link it.
        }
        snapshotFile.writeText(json.toString())
    }

    fun loadSnapshot(): ProjectSnapshot? {
        if (!snapshotFile.exists()) return null
        return try {
            val json = JSONObject(snapshotFile.readText())
            ProjectSnapshot(
                timestamp = json.getLong("timestamp"),
                indexedFileCount = json.getInt("indexedFileCount"),
                symbolCount = json.getInt("symbolCount"),
                architectureSnapshot = null // Usually loaded from ArchitectureMemory
            )
        } catch (e: Exception) {
            null
        }
    }
}
