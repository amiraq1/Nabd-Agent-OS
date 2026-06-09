package com.nabd.ai.local.memory

/**
 * MemoryRetriever: Responsible for selecting and ranking memories for context injection.
 * Ensures the most relevant information is provided within the token budget.
 */
class MemoryRetriever(private val memoryManager: MemoryManager) {

    /**
     * Retrieves the most important memories to be injected into the system prompt.
     * @param maxEntries Maximum number of memory entries to retrieve.
     * @return A formatted string containing relevant memories.
     */
    suspend fun retrieveContextualMemories(maxEntries: Int = 5): String {
        val context = memoryManager.getRelevantContext(maxEntries)
        return if (context.isNotEmpty()) {
            "--- RELEVANT LONG-TERM MEMORIES ---\n$context\n-----------------------------------"
        } else {
            ""
        }
    }
}
