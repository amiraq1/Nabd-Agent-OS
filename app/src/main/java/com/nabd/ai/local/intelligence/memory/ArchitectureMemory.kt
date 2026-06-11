package com.nabd.ai.local.intelligence.memory

import java.io.File
import org.json.JSONObject

data class ArchitectureSnapshot(
    val entryPoints: List<String>,
    val coreModules: List<String>,
    val description: String,
    val timestamp: Long
)

class ArchitectureMemory(private val memoryFile: File) {
    
    init {
        if (!memoryFile.exists()) {
            memoryFile.parentFile?.mkdirs()
            save(ArchitectureSnapshot(emptyList(), emptyList(), "Initial Architecture", System.currentTimeMillis()))
        }
    }

    fun load(): ArchitectureSnapshot? {
        if (!memoryFile.exists()) return null
        return try {
            val json = JSONObject(memoryFile.readText())
            val entryPoints = json.getJSONArray("entryPoints").let { array ->
                List(array.length()) { array.getString(it) }
            }
            val coreModules = json.getJSONArray("coreModules").let { array ->
                List(array.length()) { array.getString(it) }
            }
            ArchitectureSnapshot(
                entryPoints = entryPoints,
                coreModules = coreModules,
                description = json.getString("description"),
                timestamp = json.getLong("timestamp")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun save(snapshot: ArchitectureSnapshot) {
        val json = JSONObject().apply {
            put("entryPoints", org.json.JSONArray(snapshot.entryPoints))
            put("coreModules", org.json.JSONArray(snapshot.coreModules))
            put("description", snapshot.description)
            put("timestamp", snapshot.timestamp)
        }
        memoryFile.writeText(json.toString(2))
    }
}
