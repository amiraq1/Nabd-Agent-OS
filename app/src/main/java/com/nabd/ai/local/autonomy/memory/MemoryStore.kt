package com.nabd.ai.local.autonomy.memory

import java.time.Instant

/**
 * MemoryEntry: Represents a single unit of knowledge or experience in the agent's memory.
 */
data class MemoryEntry(
    val id: String,
    val type: MemoryType,
    val content: String,
    val timestamp: Instant,
    val metadata: Map<String, String> = emptyMap()
)

enum class MemoryType { SHORT_TERM, LONG_TERM, SUMMARY, TOOL_OUTPUT }

/**
 * MemoryStore: Manages agent's working memory and handles context compression.
 */
class MemoryStore(
    private val knowledgeMemory: KnowledgeMemory = KnowledgeMemory()
) {
    private val shortTermMemory = mutableListOf<MemoryEntry>()

    @Synchronized
    fun addEntry(entry: MemoryEntry) {
        if (entry.type == MemoryType.SHORT_TERM) {
            shortTermMemory.add(entry)
            if (shortTermMemory.size > 20) {
                compressShortTermMemory()
            }
        } else if (entry.type == MemoryType.LONG_TERM) {
            knowledgeMemory.addEntry(
                KnowledgeEntry(
                    id = entry.id,
                    category = KnowledgeCategory.OBSERVATION,
                    content = entry.content,
                    timestamp = entry.timestamp
                )
            )
        }
    }

    fun retrieveRelevantContext(query: String): List<MemoryEntry> {
        val stm = shortTermMemory
        val ltm = knowledgeMemory.search(query).map { 
            MemoryEntry(
                id = it.id,
                type = MemoryType.LONG_TERM,
                content = it.content,
                timestamp = it.timestamp,
                metadata = mapOf("category" to it.category.name)
            )
        }
        return stm + ltm
    }

    private fun compressShortTermMemory() {
        // آلية استدعاء الـ LLM لضغط أول 10 مدخلات إلى ملخص تنفيذي واحد (Summary Type)
        // In this implementation, we simulate the compression by creating a summary entry.
        val itemsToCompress = shortTermMemory.take(10)
        shortTermMemory.removeAll(itemsToCompress)
        
        val compressedSummary = MemoryEntry(
            id = "summary_${Instant.now().toEpochMilli()}",
            type = MemoryType.SUMMARY,
            content = "Summary of previous actions: Compressed due to buffer limit. Contains ${itemsToCompress.size} items.",
            timestamp = Instant.now()
        )
        addEntry(compressedSummary)
    }
}
