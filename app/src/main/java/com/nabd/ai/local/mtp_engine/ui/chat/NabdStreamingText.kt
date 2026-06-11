package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import com.nabd.ai.local.mtp_engine.domain.tools.ToolResult

/**
 * TextSegment: Represents different parts of a parsed markdown stream.
 */
sealed interface TextSegment {
    data class Plain(val text: String) : TextSegment
    data class Code(val code: String, val language: String?) : TextSegment
    data class ToolCall(val name: String, val arguments: String, val result: ToolResult? = null) : TextSegment
}

/**
 * parseMarkdownStream: A lightweight $O(N)$ parser for streaming markdown.
 * Avoids heavy regex to maintain UI thread performance during recomposition.
 */
fun parseMarkdownStream(content: String): List<TextSegment> {
    if (!content.contains("```") && !content.contains("<tool_call>")) {
        return listOf(TextSegment.Plain(content))
    }

    val segments = mutableListOf<TextSegment>()
    
    // Basic heuristic parsing for tool calls (assuming <tool_call> JSON </tool_call> format for now)
    // In a full implementation, this would be a more robust state machine parser.
    var remaining = content
    while (remaining.isNotEmpty()) {
        val toolStart = remaining.indexOf("<tool_call>")
        val codeStart = remaining.indexOf("```")

        if (toolStart != -1 && (codeStart == -1 || toolStart < codeStart)) {
            if (toolStart > 0) {
                segments.addAll(parseCodeBlocks(remaining.substring(0, toolStart)))
            }
            val toolEnd = remaining.indexOf("</tool_call>", toolStart)
            if (toolEnd != -1) {
                val toolContent = remaining.substring(toolStart + 11, toolEnd).trim()
                // Simple extraction, assuming JSON or basic format: {"name": "...", "arguments": "{...}"}
                // For UI display, we'll just show the raw content if parsing fails
                try {
                    val json = org.json.JSONObject(toolContent)
                    segments.add(TextSegment.ToolCall(
                        name = json.optString("name", "UNKNOWN"),
                        arguments = json.optString("arguments", toolContent)
                    ))
                } catch (e: Exception) {
                    segments.add(TextSegment.ToolCall(name = "PARSE_ERROR", arguments = toolContent))
                }
                remaining = remaining.substring(toolEnd + 12)
            } else {
                // Incomplete tool call
                segments.add(TextSegment.ToolCall(name = "STREAMING...", arguments = remaining.substring(toolStart + 11)))
                break
            }
        } else if (codeStart != -1) {
            segments.addAll(parseCodeBlocks(remaining))
            break // parseCodeBlocks handles the rest
        } else {
            segments.add(TextSegment.Plain(remaining.trim('\n')))
            break
        }
    }
    
    return segments
}

private fun parseCodeBlocks(content: String): List<TextSegment> {
    val segments = mutableListOf<TextSegment>()
    val parts = content.split("```")
    
    parts.forEachIndexed { index, part ->
        if (index % 2 == 0) { // Plain text
            if (part.isNotBlank()) segments.add(TextSegment.Plain(part.trim('\n')))
        } else { // Code block
            val lines = part.lines()
            val language = lines.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
            val code = lines.drop(1).joinToString("\n").trimEnd()
            segments.add(TextSegment.Code(code, language))
        }
    }
    return segments
}

/**
 * NabdStreamingText: Zero-Jank Stream Parsing and Rendering.
 * Handles mixed content (text and code) with precise throttling.
 */
@Composable
fun NabdStreamingText(
    content: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD4D4D8),
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    var displayedText by remember { mutableStateOf(content) }

    LaunchedEffect(content, isStreaming) {
        if (isStreaming) {
            delay(60) // 60fps throttle
            displayedText = content
        } else {
            displayedText = content
        }
    }

    Column(modifier = modifier) {
        val segments = remember(displayedText) { parseMarkdownStream(displayedText) }
        
        segments.forEach { segment ->
            when (segment) {
                is TextSegment.Plain -> {
                    Text(
                        text = segment.text,
                        style = TextStyle(
                            color = color,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            lineHeight = 28.sp,
                            fontFamily = FontFamily.Default
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                is TextSegment.Code -> {
                    NabdCodeBlock(
                        code = segment.code,
                        language = segment.language,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                is TextSegment.ToolCall -> {
                    NabdToolCallBlock(
                        toolName = segment.name,
                        arguments = segment.arguments,
                        result = segment.result,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
