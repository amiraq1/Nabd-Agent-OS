package com.nabd.ai.local.autonomy.permission

import com.nabd.ai.local.agent.ToolRiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

enum class PermissionCategory { TERMINAL, FILE_SYSTEM, WEB, MEMORY, DEVICE, EXTERNAL_API }
enum class PermissionStatus { GRANTED, DENIED, ASK_EVERY_TIME }

/**
 * ToolPermissionManager: Manages permissions and user approvals for sensitive tool categories.
 *
 * Maps agent-level [com.nabd.ai.local.agent.ToolCategory] + [ToolRiskLevel] to
 * [PermissionCategory] so the orchestrator can enforce permission checks without
 * breaking existing tool definitions.
 */
class ToolPermissionManager {
    private val permissionRegistry = mutableMapOf<PermissionCategory, PermissionStatus>()
    val authPromptRequest = MutableStateFlow<PermissionPromptRequest?>(null)

    data class PermissionPromptRequest(
        val toolName: String,
        val category: PermissionCategory,
        val onDecision: (Boolean) -> Unit
    )

    /**
     * Updates the permission status for a specific category.
     */
    fun setPermission(category: PermissionCategory, status: PermissionStatus) {
        permissionRegistry[category] = status
    }

    /**
     * Verifies if a tool has permission to execute, prompting the user if necessary.
     */
    suspend fun verifyAndRequestPermission(toolName: String, category: PermissionCategory): Boolean {
        return when (permissionRegistry[category] ?: PermissionStatus.ASK_EVERY_TIME) {
            PermissionStatus.GRANTED -> true
            PermissionStatus.DENIED -> false
            PermissionStatus.ASK_EVERY_TIME -> {
                requestUserApproval(toolName, category)
            }
        }
    }

    /**
     * Checks if a tool requires explicit permission based on its risk level.
     * SAFE and LOW risk tools are auto-granted; MODERATE+ require permission checks.
     */
    fun requiresPermissionCheck(riskLevel: ToolRiskLevel): Boolean {
        return riskLevel.ordinal >= ToolRiskLevel.MODERATE.ordinal
    }

    private suspend fun requestUserApproval(toolName: String, category: PermissionCategory): Boolean {
        return suspendCancellableCoroutine { continuation ->
            authPromptRequest.value = PermissionPromptRequest(toolName, category) { approved ->
                authPromptRequest.value = null
                continuation.resume(approved)
            }
        }
    }

    companion object {
        /**
         * Maps an agent-level ToolCategory to a PermissionCategory.
         */
        fun mapToPermissionCategory(
            agentCategory: com.nabd.ai.local.agent.ToolCategory
        ): PermissionCategory {
            return when (agentCategory) {
                com.nabd.ai.local.agent.ToolCategory.SYSTEM -> PermissionCategory.TERMINAL
                com.nabd.ai.local.agent.ToolCategory.FILESYSTEM -> PermissionCategory.FILE_SYSTEM
                com.nabd.ai.local.agent.ToolCategory.COMMUNICATION -> PermissionCategory.WEB
                com.nabd.ai.local.agent.ToolCategory.KNOWLEDGE -> PermissionCategory.MEMORY
                com.nabd.ai.local.agent.ToolCategory.UTILITY -> PermissionCategory.DEVICE
            }
        }
    }
}
