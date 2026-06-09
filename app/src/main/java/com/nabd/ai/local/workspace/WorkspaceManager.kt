package com.nabd.ai.local.workspace

import java.io.File

interface WorkspaceManager {
    val workspaceRoot: File
    
    /**
     * Resolves a relative path to a secure, canonical file within the workspace.
     * Throws SecurityException if the path attempts to break out of the workspace sandbox.
     */
    fun resolvePath(relativePath: String): File
    
    /**
     * Validates that a given file resides securely within the workspace.
     */
    fun validatePath(file: File)
    
    /**
     * Returns true if the file exists and is within the workspace.
     */
    fun exists(relativePath: String): Boolean
}
