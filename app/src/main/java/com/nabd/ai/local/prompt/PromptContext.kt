package com.nabd.ai.local.prompt

data class PromptContext(
    val systemPrompt: String,
    val toolDefinitions: String,
    val memoryContext: String,
    val knowledgeContext: String,
    val conversationHistory: List<String>,
    val userInput: String
)
