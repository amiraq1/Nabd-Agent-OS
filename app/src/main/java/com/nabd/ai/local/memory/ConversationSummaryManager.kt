package com.nabd.ai.local.memory

import com.nabd.ai.local.engine.LlmProvider
import kotlinx.coroutines.flow.first

/**
 * ConversationSummaryManager: Manages the lifecycle of conversation summaries.
 * When a conversation becomes too long, it summarizes old messages and stores them in memory.
 */
class ConversationSummaryManager(
    private val memoryManager: MemoryManager,
    private val provider: LlmProvider
) {
    /**
     * Summarizes a list of messages and stores the result as a SUMMARY memory type.
     * @param conversationId The ID of the conversation to summarize.
     * @param messages The text of the messages to summarize.
     */
    suspend fun summarizeAndStore(conversationId: String, messages: String) {
        // In a real implementation, we would call the LLM to generate a summary.
        // For this milestone, we simulate the summarization.
        val summaryPrompt = "Summarize the following conversation briefly:\n$messages"
        
        // This would be an internal LLM call that doesn't stream to the UI
        // val summary = provider.generateText(summaryPrompt).first() 
        val summary = "Simulated summary of conversation $conversationId: User discussed local LLM integration and memory systems."

        memoryManager.storeMemory(
            type = MemoryType.SUMMARY,
            content = summary,
            importance = 7,
            conversationId = conversationId
        )
    }
}
