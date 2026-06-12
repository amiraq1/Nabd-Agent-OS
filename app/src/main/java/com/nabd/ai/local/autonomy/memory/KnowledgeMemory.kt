package com.nabd.ai.local.autonomy.memory

import java.time.Instant

/**
 * KnowledgeEntry: A persistent piece of information learned by the agent.
 */
data class KnowledgeEntry(
    val id: String,
    val category: KnowledgeCategory,
    val content: String,
    val source: String? = null,
    val timestamp: Instant = Instant.now(),
    val importance: Float = 0.5f,
    val tags: List<String> = emptyList()
)

enum class KnowledgeCategory { USER_PREFERENCE, PROJECT_FACT, LEARNED_SOLUTION, TOOL_PATTERN, OBSERVATION }

/**
 * KnowledgeMemory: Manages long-term persistent knowledge.
 */
class KnowledgeMemory {
    private val entries = mutableMapOf<String, KnowledgeEntry>()

    fun addEntry(entry: KnowledgeEntry) {
        entries[entry.id] = entry
    }

    fun getEntriesByCategory(category: KnowledgeCategory): List<KnowledgeEntry> {
        return entries.values.filter { it.category == category }
    }

    /**
     * Search for relevant knowledge based on a query string.
     * (Placeholder for semantic/vector search)
     */
    fun search(query: String): List<KnowledgeEntry> {
        return entries.values.filter { entry ->
            entry.content.contains(query, ignoreCase = true) || 
            entry.tags.any { it.contains(query, ignoreCase = true) }
        }.sortedByDescending { it.importance }
    }

    fun consolidate() {
        // Periodic summarization and cleanup of low-importance entries
        // In a real implementation, this would use an LLM to merge similar facts
    }
}
