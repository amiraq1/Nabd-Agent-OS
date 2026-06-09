package com.nabd.ai.local.prompt.templates

interface ChatTemplate {
    val name: String
    fun format(systemPrompt: String, history: List<Pair<String, String>>, newUserInput: String): String
}

class ChatTemplateManager(
    private var activeTemplate: ChatTemplate = Llama3Template()
) {
    fun setActiveTemplate(templateName: String) {
        activeTemplate = when (templateName.lowercase()) {
            "gemma" -> GemmaTemplate()
            "qwen" -> QwenTemplate()
            else -> Llama3Template()
        }
    }

    fun format(systemPrompt: String, history: List<Pair<String, String>>, newUserInput: String): String {
        return activeTemplate.format(systemPrompt, history, newUserInput)
    }
}
