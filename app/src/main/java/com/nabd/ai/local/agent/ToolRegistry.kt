package com.nabd.ai.local.agent

/**
 * ToolRegistry: Centralized repository for all available agent tools.
 */
class ToolRegistry {
    private val tools = mutableMapOf<String, ToolDefinition>()

    fun register(tool: ToolDefinition) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): ToolDefinition? = tools[name]

    fun getAllTools(): List<ToolDefinition> = tools.values.toList()

    /**
     * Generates a JSON string representing the schemas of all registered tools.
     * This is used for dynamic system prompt injection.
     */
    fun generateSystemPromptSchema(): String {
        return tools.values.joinToString("\n\n") { tool ->
            """
            Tool: ${tool.name}
            Description: ${tool.description}
            Schema: ${tool.jsonSchema}
            Requires Approval: ${tool.requiresHumanApproval}
            """.trimIndent()
        }
    }
}
