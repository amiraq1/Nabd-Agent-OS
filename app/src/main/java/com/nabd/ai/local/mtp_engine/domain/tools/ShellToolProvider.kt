package com.nabd.ai.local.mtp_engine.domain.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ShellToolProvider: Tactical CLI Execution.
 * Intentional Minimalism - Executes commands in the host environment (Termux).
 */
class ShellToolProvider(
    private val workingDirectory: File
) : ToolProvider {
    
    override val name: String = "execute_shell_command"

    override fun canHandle(toolName: String): Boolean = toolName == name

    override suspend fun execute(arguments: String): ToolResult = withContext(Dispatchers.IO) {
        try {
            val command = extractCommand(arguments)
            
            // تنفيذ الأوامر الطرفية مباشرة داخل بيئة Termux المضيفة
            val process = ProcessBuilder()
                .command("sh", "-c", command)
                .directory(workingDirectory)
                .redirectErrorStream(true) // دمج مخرجات الخطأ مع المخرجات القياسية
                .start()

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
