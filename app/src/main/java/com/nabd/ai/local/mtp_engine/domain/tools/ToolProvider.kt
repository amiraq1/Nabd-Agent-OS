package com.nabd.ai.local.mtp_engine.domain.tools

/**
 * ToolProvider: Interface for pluggable tactical tool execution.
 * Part of the MTP Tool Domain.
 */
interface ToolProvider {
    val name: String
    
    /**
     * التحقق السريع من قدرة الأداة على معالجة الطلب بـ O(1)
     */
    fun canHandle(toolName: String): Boolean
    
    /**
     * تنفيذ تكتيكي معزول لا يوقف خيط واجهة المستخدم
     */
    suspend fun execute(arguments: String): ToolResult
}

/**
 * ToolResult: Represents the outcome of a tactical tool execution.
 */
sealed interface ToolResult {
    data class Success(val output: String) : ToolResult
    data class Failure(val reason: String) : ToolResult
}
