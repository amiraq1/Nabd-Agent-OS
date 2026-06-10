package com.nabd.ai.local.workspace

import java.io.File
import java.io.IOException

class LocalSandboxManager(
    override val workspaceRoot: File
) : WorkspaceManager {

    init {
        if (!workspaceRoot.exists()) {
            workspaceRoot.mkdirs()
        }
    }

    override fun resolvePath(relativePath: String): File {
        if (relativePath.contains("..")) {
            throw SecurityException("Path traversal attempt detected: $relativePath")
        }
        val targetFile = File(workspaceRoot, relativePath)
        validatePath(targetFile)
        return targetFile
    }

    override fun validatePath(file: File) {
        val rootCanonical = workspaceRoot.canonicalPath
        val targetCanonical = file.canonicalPath

        if (!targetCanonical.startsWith(rootCanonical)) {
            throw SecurityException("Path traversal attempt detected. Access denied to path outside workspace: ${file.path}")
        }
    }

    override fun exists(relativePath: String): Boolean {
        return try {
            resolvePath(relativePath).exists()
        } catch (e: SecurityException) {
            false
        } catch (e: IOException) {
            false
        }
    }
}
