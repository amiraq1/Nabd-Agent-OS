package com.nabd.ai.local.autonomy.persistence

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * JournalEntry: A record of an atomic change in the agent's state.
 */
data class JournalEntry(
    val id: Long = System.currentTimeMillis(),
    val planId: UUID,
    val type: JournalEventType,
    val data: String
)

enum class JournalEventType { STEP_STARTED, STEP_COMPLETED, STEP_FAILED, MEMORY_UPDATED, REPLAN_TRIGGERED }

/**
 * ExecutionJournal: Persists every state change to disk for recovery.
 * Uses append-only JSONL (JSON Lines) for crash-safe writes.
 */
class ExecutionJournal(private val journalDir: File) {
    private val entries = mutableListOf<JournalEntry>()

    init {
        if (!journalDir.exists()) journalDir.mkdirs()
    }

    fun recordEvent(planId: UUID, type: JournalEventType, data: String) {
        val entry = JournalEntry(planId = planId, type = type, data = data)
        entries.add(entry)

        // Append to JSONL file for crash safety
        try {
            val journalFile = File(journalDir, "${planId}.jsonl")
            val line = JSONObject().apply {
                put("id", entry.id)
                put("planId", planId.toString())
                put("type", type.name)
                put("data", data)
            }.toString()
            journalFile.appendText(line + "\n")
        } catch (e: Exception) {
            Log.e("ExecutionJournal", "Failed to persist journal entry", e)
        }
    }

    fun getEventsForPlan(planId: UUID): List<JournalEntry> {
        // First check in-memory cache
        val cached = entries.filter { it.planId == planId }
        if (cached.isNotEmpty()) return cached

        // Then try to load from disk
        return loadFromDisk(planId)
    }

    private fun loadFromDisk(planId: UUID): List<JournalEntry> {
        val journalFile = File(journalDir, "${planId}.jsonl")
        if (!journalFile.exists()) return emptyList()

        return try {
            journalFile.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        val json = JSONObject(line)
                        JournalEntry(
                            id = json.getLong("id"),
                            planId = UUID.fromString(json.getString("planId")),
                            type = JournalEventType.valueOf(json.getString("type")),
                            data = json.getString("data")
                        )
                    } catch (_: Exception) { null }
                }
        } catch (e: Exception) {
            Log.e("ExecutionJournal", "Failed to load journal from disk", e)
            emptyList()
        }
    }
}

/**
 * CheckpointManager: Handles full snapshots of the system state for crash recovery.
 * Saves checkpoints as JSON files with timestamps for versioning.
 */
class CheckpointManager(private val checkpointDir: File) {

    init {
        if (!checkpointDir.exists()) checkpointDir.mkdirs()
    }

    /**
     * Creates a checkpoint by serializing the state map to a timestamped JSON file.
     * @param state A map of key-value pairs representing the system state.
     */
    fun createCheckpoint(state: Map<String, Any>) {
        try {
            val timestamp = System.currentTimeMillis()
            val checkpointFile = File(checkpointDir, "checkpoint_$timestamp.json")

            val json = JSONObject().apply {
                put("timestamp", timestamp)
                put("version", 1)

                val stateObj = JSONObject()
                for ((key, value) in state) {
                    when (value) {
                        is String -> stateObj.put(key, value)
                        is Number -> stateObj.put(key, value)
                        is Boolean -> stateObj.put(key, value)
                        is List<*> -> stateObj.put(key, JSONArray(value))
                        else -> stateObj.put(key, value.toString())
                    }
                }
                put("state", stateObj)
            }

            checkpointFile.writeText(json.toString(2))
            Log.i("CheckpointManager", "Checkpoint created: ${checkpointFile.name}")

            // Keep only the last 5 checkpoints to save storage
            pruneOldCheckpoints(maxKeep = 5)
        } catch (e: Exception) {
            Log.e("CheckpointManager", "Failed to create checkpoint", e)
        }
    }

    /**
     * Restores the most recent checkpoint as a key-value map.
     * @return The state map, or null if no checkpoints exist.
     */
    fun restoreFromLatestCheckpoint(): Map<String, Any>? {
        try {
            val latestFile = checkpointDir.listFiles { _, name -> name.startsWith("checkpoint_") && name.endsWith(".json") }
                ?.maxByOrNull { it.lastModified() }
                ?: return null

            val json = JSONObject(latestFile.readText())
            val stateObj = json.getJSONObject("state")

            val result = mutableMapOf<String, Any>()
            for (key in stateObj.keys()) {
                result[key] = stateObj.get(key)
            }

            Log.i("CheckpointManager", "Restored checkpoint from: ${latestFile.name}")
            return result
        } catch (e: Exception) {
            Log.e("CheckpointManager", "Failed to restore checkpoint", e)
            return null
        }
    }

    /**
     * Returns the timestamp of the latest checkpoint, or null if none exist.
     */
    fun getLatestCheckpointTimestamp(): Long? {
        return checkpointDir.listFiles { _, name -> name.startsWith("checkpoint_") }
            ?.maxByOrNull { it.lastModified() }
            ?.let {
                try {
                    JSONObject(it.readText()).getLong("timestamp")
                } catch (_: Exception) { null }
            }
    }

    private fun pruneOldCheckpoints(maxKeep: Int) {
        val files = checkpointDir.listFiles { _, name -> name.startsWith("checkpoint_") && name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        if (files.size > maxKeep) {
            files.drop(maxKeep).forEach { file ->
                file.delete()
                Log.d("CheckpointManager", "Pruned old checkpoint: ${file.name}")
            }
        }
    }
}

