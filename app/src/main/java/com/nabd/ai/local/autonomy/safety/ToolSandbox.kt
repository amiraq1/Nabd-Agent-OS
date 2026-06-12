package com.nabd.ai.local.autonomy.safety

import android.util.Log
import java.io.File

/**
 * ToolSandbox: Enforces security policies and filesystem boundaries on tool execution.
 */
class ToolSandbox(
    private val workspaceRoot: File,
    private val blockedCommands: List<String> = listOf("rm -rf", "chmod 777", "killall", "dd if=", "mkfs")
) {
    /**
     * Validates if a command is safe to execute.
     */
    fun validateCommand(command: String): Boolean {
        for (blocked in blockedCommands) {
            if (command.contains(blocked, ignoreCase = true)) {
                Log.e("ToolSandbox", "Blocked dangerous command: $command")
                return false
            }
        }
        return true
    }

    /**
     * Validates if a file path is within the allowed workspace boundary.
     */
    fun validatePath(path: String): Boolean {
        val file = File(path)
        val absolutePath = if (file.isAbsolute) file.canonicalPath else File(workspaceRoot, path).canonicalPath
        
        return if (absolutePath.startsWith(workspaceRoot.canonicalPath)) {
            true
        } else {
            Log.e("ToolSandbox", "Path escape attempt: $path (resolved to $absolutePath)")
            false
        }
    }

    /**
     * Audit log for tool execution attempts.
     */
    fun auditLog(toolName: String, parameters: String, allowed: Boolean) {
        val status = if (allowed) "ALLOWED" else "REJECTED"
        Log.i("ToolSandboxAudit", "[$status] Tool: $toolName | Params: $parameters")
    }
}
