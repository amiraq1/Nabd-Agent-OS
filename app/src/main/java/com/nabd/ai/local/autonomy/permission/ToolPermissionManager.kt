package com.nabd.ai.local.autonomy.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

enum class ToolCategory { TERMINAL, FILE_SYSTEM, WEB, MEMORY, DEVICE, EXTERNAL_API }
enum class PermissionStatus { GRANTED, DENIED, ASK_EVERY_TIME }

/**
 * ToolPermissionManager: Manages permissions and user approvals for sensitive tool categories.
 */
class ToolPermissionManager {
    private val permissionRegistry = mutableMapOf<ToolCategory, PermissionStatus>()
    val authPromptRequest = MutableStateFlow<PermissionPromptRequest?>(null)

    data class PermissionPromptRequest(
        val toolName: String,
        val category: ToolCategory,
        val onDecision: (Boolean) -> Unit
    )

    /**
     * Updates the permission status for a specific category.
     */
    fun setPermission(category: ToolCategory, status: PermissionStatus) {
        permissionRegistry[category] = status
    }

    /**
     * Verifies if a tool has permission to execute, prompting the user if necessary.
     */
    suspend fun verifyAndRequestPermission(toolName: String, category: ToolCategory): Boolean {
        return when (permissionRegistry[category] ?: PermissionStatus.ASK_EVERY_TIME) {
            PermissionStatus.GRANTED -> true
            PermissionStatus.DENIED -> false
            PermissionStatus.ASK_EVERY_TIME -> {
                requestUserApproval(toolName, category)
            }
        }
    }

    private suspend fun requestUserApproval(toolName: String, category: ToolCategory): Boolean {
        return suspendCancellableCoroutine { continuation ->
            authPromptRequest.value = PermissionPromptRequest(toolName, category) { approved ->
                authPromptRequest.value = null
                continuation.resume(approved)
            }
        }
    }
}
