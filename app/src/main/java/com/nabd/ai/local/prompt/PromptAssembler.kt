package com.nabd.ai.local.prompt

import com.nabd.ai.local.prompt.templates.ChatTemplateManager

/**
 * PromptAssembler: Centralized assembly pipeline that merges various contexts
 * and applies the Chat Template and Context Window strategies.
 */
class PromptAssembler(
    private val strategy: ContextWindowStrategy,
    private val templateManager: ChatTemplateManager
) {
    fun assemble(context: PromptContext): String {
        // 1. Enforce Token Limits
        val constrainedContext = strategy.applyStrategy(context)

        // 2. Assemble logical blocks
        val fullSystemPrompt = buildString {
            append(constrainedContext.systemPrompt)
            if (constrainedContext.toolDefinitions.isNotBlank()) {
                append("\n\nAVAILABLE TOOLS:\n")
                append(constrainedContext.toolDefinitions)
            }
            if (constrainedContext.knowledgeContext.isNotBlank()) {
                append("\n\nKNOWLEDGE BASE:\n")
                append(constrainedContext.knowledgeContext)
            }
            if (constrainedContext.memoryContext.isNotBlank()) {
                append("\n\nRELEVANT MEMORIES:\n")
                append(constrainedContext.memoryContext)
            }
        }

        // 3. Format using model-specific Chat Template
        val formattedHistory = constrainedContext.conversationHistory.map { msg ->
            // Simulating parsing history into role/content for template
            val role = if (msg.startsWith("User:")) "user" else "assistant"
            val content = msg.removePrefix("User: ").removePrefix("Assistant: ").trim()
            role to content
        }

        return templateManager.format(
            systemPrompt = fullSystemPrompt,
            history = formattedHistory,
            newUserInput = constrainedContext.userInput
        )
    }
}
