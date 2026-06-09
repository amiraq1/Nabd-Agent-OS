package com.nabd.ai.local.prompt.templates

class GemmaTemplate : ChatTemplate {
    override val name = "gemma"

    override fun format(systemPrompt: String, history: List<Pair<String, String>>, newUserInput: String): String {
        return buildString {
            // Gemma typically puts system prompt in the first user message or as a system turn depending on version.
            // Assuming standard format:
            if (systemPrompt.isNotBlank()) {
                append("<start_of_turn>user\n")
                append(systemPrompt)
                append("\n<end_of_turn>\n")
                append("<start_of_turn>model\n")
                append("Understood.\n<end_of_turn>\n")
            }
            
            for ((role, content) in history) {
                val gemmaRole = if (role == "assistant") "model" else "user"
                append("<start_of_turn>$gemmaRole\n")
                append(content)
                append("\n<end_of_turn>\n")
            }
            
            append("<start_of_turn>user\n")
            append(newUserInput)
            append("\n<end_of_turn>\n<start_of_turn>model\n")
        }
    }
}
