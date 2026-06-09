package com.nabd.ai.local.prompt

import com.nabd.ai.local.prompt.templates.ChatTemplateManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PromptAssemblerTest {

    @Test
    fun `assemble truncates appropriately based on strategy`() {
        val strategy = ContextWindowStrategy(maxTokens = 50) // Very tight budget
        val manager = ChatTemplateManager()
        manager.setActiveTemplate("llama3")
        
        val assembler = PromptAssembler(strategy, manager)
        
        val context = PromptContext(
            systemPrompt = "System",
            toolDefinitions = "Tools",
            memoryContext = "Memory that is extremely long and should be truncated because it is way too large for the budget",
            knowledgeContext = "Knowledge that is also extremely long and should be truncated",
            conversationHistory = listOf("User: Hi", "Assistant: Hello"),
            userInput = "Test"
        )
        
        val assembled = assembler.assemble(context)
        
        // Ensure it doesn't throw and formats properly
        assertTrue(assembled.contains("<|start_header_id|>system<|end_header_id|>"))
        // Memory and Knowledge should be severely truncated or empty
        assertTrue(assembled.length < 500) 
    }
}
