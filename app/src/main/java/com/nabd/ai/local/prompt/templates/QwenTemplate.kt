package com.nabd.ai.local.prompt.templates

class QwenTemplate : ChatTemplate {
    override val name = "qwen"

    override fun format(systemPrompt: String, history: List<Pair<String, String>>, newUserInput: String): String {
        return buildString {
            if (systemPrompt.isNotBlank()) {
                append("<|im_start|>system\n")
                append(systemPrompt)
                append("<|im_end|>\n")
            }
            
            for ((role, content) in history) {
                append("<|im_start|>$role\n")
                append(content)
                append("<|im_end|>\n")
            }
            
            append("<|im_start|>user\n")
            append(newUserInput)
            append("<|im_end|>\n<|im_start|>assistant\n")
        }
    }
}
