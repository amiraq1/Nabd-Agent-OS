package com.nabd.ai.local.agent

/**
 * ToolRiskLevel: Categorizes the potential impact of a tool.
 */
enum class ToolRiskLevel {
    SAFE,       // Read-only or idempotent operations (e.g., Time, Calculator)
    ELEVATED,   // External access (e.g., Web Search)
    DANGEROUS   // Write access or system modification (e.g., File Write, Shell)
}

/**
 * ToolCategory: Functional grouping of tools.
 */
enum class ToolCategory {
    SYSTEM,
    UTILITY,
    KNOWLEDGE,
    COMMUNICATION,
    FILESYSTEM
}

/**
 * ToolDefinition: The strict contract for any tool that can be called by the Agent.
 */
interface ToolDefinition {
    val name: String
    val description: String
    val jsonSchema: String // JSON Schema for parameters
    val category: ToolCategory
    val riskLevel: ToolRiskLevel
    val requiresHumanApproval: Boolean
        get() = riskLevel == ToolRiskLevel.DANGEROUS

    /**
     * Executes the tool with the provided JSON parameters.
     * @param params JSON string containing the arguments.
     * @return Result containing the observation or error.
     */
    suspend fun execute(params: String): Result<String>
}
