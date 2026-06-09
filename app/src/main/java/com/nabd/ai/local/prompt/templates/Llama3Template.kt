package com.nabd.ai.local.prompt.templates

class Llama3Template : ChatTemplate {
    override val name = "llama3"

    override fun format(systemPrompt: String, history: List<Pair<String, String>>, newUserInput: String): String {
        return buildString {
            append("<|begin_of_text|>")
            if (systemPrompt.isNotBlank()) {
                append("<|start_header_id|>system<|end_header_id|>\n\n")
                append(systemPrompt)
                append("<|eot_id|>")
            }
            
            for ((role, content) in history) {
                append("<|start_header_id|>$role<|end_header_id|>\n\n")
                append(content)
                append("<|eot_id|>")
            }
            
            append("<|start_header_id|>user<|end_header_id|>\n\n")
            append(newUserInput)
            append("<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n")
        }
    }
}
