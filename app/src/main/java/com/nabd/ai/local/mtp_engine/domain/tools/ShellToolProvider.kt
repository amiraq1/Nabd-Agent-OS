package com.nabd.ai.local.mtp_engine.domain.tools

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ShellToolProvider: Tactical CLI Execution.
 * Executes commands in the host environment (Termux) with
 * environment variable propagation for secure credential access.
 */
class ShellToolProvider(
    private val workingDirectory: File
) : ToolProvider {
    
    override val name: String = "execute_shell_command"

    override fun canHandle(toolName: String): Boolean = toolName == name

    override suspend fun execute(arguments: String): ToolResult = withContext(Dispatchers.IO) {
        try {
            val command = extractCommand(arguments)
            
            val pb = ProcessBuilder()
                .command("sh", "-c", command)
                .directory(workingDirectory)
                .redirectErrorStream(true)

            // Propagate critical environment variables to child processes.
            // Existence is logged; the value itself is never exposed.
            pb.environment().let { env ->
                System.getenv("NVIDIA_NIM_API_KEY")?.let { key ->
                    if (key.isNotBlank()) {
                        env["NVIDIA_NIM_API_KEY"] = key
                        Log.i("ShellToolProvider", "NVIDIA_NIM_API_KEY detected")
                    }
                }
            }

            val process = pb.start()

            // قراءة المخرجات بفعالية
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
            val exitCode = process.waitFor()

            // اقتطاع المخرجات لحماية نافذة سياق المحرك المحلي
            val safeOutput = if (output.length > 2000) {
                output.take(2000) + "\n...[TRUNCATED_TO_PRESERVE_CONTEXT]"
            } else {
                output
            }

            if (exitCode == 0) {
                ToolResult.Success(safeOutput.ifEmpty { "EXEC_SUCCESS :: NO_OUTPUT" })
            } else {
                ToolResult.Failure("PROCESS_FAULT :: EXIT_CODE_$exitCode :: $safeOutput")
            }
        } catch (e: Exception) {
            ToolResult.Failure("SHELL_EXECUTION_FAULT :: ${e.message}")
        }
    }

    private fun extractCommand(arguments: String): String {
        // محلل سريع O(1) لاستخراج الأمر من هيكل JSON المتدفق
        return arguments.substringAfter("\"command\":").substringBeforeLast("}").trim(' ', '"', '\n')
    }
}
