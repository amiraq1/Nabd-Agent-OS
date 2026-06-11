package com.nabd.ai.local.mtp_engine.domain.tools

import org.json.JSONObject

/**
 * StreamingToolParser: Zero-Jank Stateful Stream Parsing for Tool Calls.
 * Intercepts the live stream, buffers minimal context, and returns a ToolCommand when complete.
 */
class StreamingToolParser {
    private val buffer = StringBuilder()
    private var inToolBlock = false

    /**
     * يعترض التدفق الحي: يعيد (null) طالما لم يكتمل الأمر، أو (ToolCommand) عند الالتقاط
     */
    fun processToken(token: String): ToolCommand? {
        buffer.append(token)
        val currentText = buffer.toString()

        if (!inToolBlock && currentText.contains("<tool_call>")) {
            inToolBlock = true
            // تفريغ المخزن للاحتفاظ بـ JSON فقط، توفيراً للذاكرة
            val jsonStart = currentText.substringAfter("<tool_call>")
            buffer.clear().append(jsonStart)
            return null
        }

        if (inToolBlock && currentText.contains("</tool_call>")) {
            val payload = currentText.substringBefore("</tool_call>").trim()
            reset()
            return parsePayload(payload)
        }

        return null
    }

    private fun reset() {
        buffer.clear()
        inToolBlock = false
    }

    private fun parsePayload(jsonString: String): ToolCommand {
        // الاعتماد على تحليل JSON خفيف وسريع لاستخراج اسم الأداة ومعاملاتها
        return try {
            val json = JSONObject(jsonString)
            val name = json.optString("name", "UNKNOWN")
            val arguments = json.optString("arguments", jsonString)
            ToolCommand(name = name, arguments = arguments)
        } catch (e: Exception) {
            ToolCommand(name = "PARSE_ERROR", arguments = jsonString)
        }
    }
}

/**
 * Represents an extracted tool command ready for orchestration.
 */
data class ToolCommand(val name: String, val arguments: String)
