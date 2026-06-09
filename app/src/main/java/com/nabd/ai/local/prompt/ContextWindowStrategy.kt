package com.nabd.ai.local.prompt

/**
 * ContextWindowStrategy: Manages token budgeting to prevent exceeding model context lengths.
 */
class ContextWindowStrategy(
    private val maxTokens: Int = 2048,
    private val tokenEstimator: (String) -> Int = { it.length / 4 } // Rough heuristic
) {
    /**
     * Truncates the context to fit within the max tokens.
     * Priorities: System (1) > Tools (2) > Input (3) > Knowledge (4) > Memory (5) > History (6)
     */
    fun applyStrategy(context: PromptContext): PromptContext {
        val systemTokens = tokenEstimator(context.systemPrompt)
        val toolTokens = tokenEstimator(context.toolDefinitions)
        val inputTokens = tokenEstimator(context.userInput)
        
        var remainingTokens = maxTokens - (systemTokens + toolTokens + inputTokens)
        if (remainingTokens <= 0) {
            // Extreme case: even essentials don't fit. Return minimal viable subset.
            return context.copy(memoryContext = "", knowledgeContext = "", conversationHistory = emptyList())
        }

        // Budget for Knowledge
        val knowledgeTokens = tokenEstimator(context.knowledgeContext)
        val finalKnowledge = if (knowledgeTokens > remainingTokens * 0.4) { // Allocate max 40% of remainder to knowledge
            val allowedChars = (remainingTokens * 0.4 * 4).toInt()
            context.knowledgeContext.take(allowedChars)
        } else {
            context.knowledgeContext
        }
        remainingTokens -= tokenEstimator(finalKnowledge)

        // Budget for Memory
        val memoryTokens = tokenEstimator(context.memoryContext)
        val finalMemory = if (memoryTokens > remainingTokens * 0.4) {
            val allowedChars = (remainingTokens * 0.4 * 4).toInt()
            context.memoryContext.take(allowedChars)
        } else {
            context.memoryContext
        }
        remainingTokens -= tokenEstimator(finalMemory)

        // The rest goes to history, pruning oldest first
        val finalHistory = mutableListOf<String>()
        for (message in context.conversationHistory.reversed()) {
            val msgTokens = tokenEstimator(message)
            if (remainingTokens >= msgTokens) {
                finalHistory.add(0, message)
                remainingTokens -= msgTokens
            } else {
                break
            }
        }

        return context.copy(
            knowledgeContext = finalKnowledge,
            memoryContext = finalMemory,
            conversationHistory = finalHistory
        )
    }
}
