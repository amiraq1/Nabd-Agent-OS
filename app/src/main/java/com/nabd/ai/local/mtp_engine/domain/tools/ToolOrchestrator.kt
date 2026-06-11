package com.nabd.ai.local.mtp_engine.domain.tools

/**
 * ToolOrchestrator: Central router for autonomous tool execution.
 * Intentional Minimalism - Autonomous Execution Routing.
 */
class ToolOrchestrator(
    private val providers: List<ToolProvider>
) {
    /**
     * توجيه تكتيكي بـ O(N) لعدد الأدوات، و O(1) للتنفيذ
     */
    suspend fun dispatch(toolName: String, arguments: String): ToolResult {
        val provider = providers.firstOrNull { it.canHandle(toolName) }
            ?: return ToolResult.Failure("TOOL_NOT_FOUND :: $toolName")

        return try {
            provider.execute(arguments)
        } catch (e: Exception) {
            ToolResult.Failure("EXECUTION_FAULT :: ${e.message}")
        }
    }
}
