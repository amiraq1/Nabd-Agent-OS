package com.nabd.ai.local.mtp_engine.domain.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * MemoryFileSystemTool: Zero-Blocking Local File Access.
 * Part of the MTP Tool Implementations.
 */
class MemoryFileSystemTool(private val rootDir: File) : ToolProvider {
    override val name: String = "read_memory_file"

    override fun canHandle(toolName: String): Boolean = toolName == name

    override suspend fun execute(arguments: String): ToolResult = withContext(Dispatchers.IO) {
        // حماية صارمة ضد اختراق المسارات (Path Traversal)
        val targetFile = File(rootDir, arguments).canonicalFile
        if (!targetFile.path.startsWith(rootDir.canonicalPath)) {
            return@withContext ToolResult.Failure("SECURITY_VIOLATION :: INVALID_PATH")
        }

        if (!targetFile.exists()) return@withContext ToolResult.Failure("FILE_MISSING :: $arguments")

        ToolResult.Success(targetFile.readText())
    }
}
