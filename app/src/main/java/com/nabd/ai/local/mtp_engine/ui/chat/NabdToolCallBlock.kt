package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.mtp_engine.domain.tools.ToolResult

/**
 * NabdToolCallBlock: Brutalist Tool Execution Block.
 * Intentional Minimalism - Sharp borders, zero radius, tactical feedback.
 */
@Composable
fun NabdToolCallBlock(
    toolName: String,
    arguments: String,
    result: ToolResult?,
    modifier: Modifier = Modifier
) {
    // التكوين المكاني: صندوق بألوان أحادية، حدود دقيقة، بدون زوايا دائرية (Radius = 0)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F11))
            .border(1.dp, Color(0xFF2A2A30))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "EXEC :: $toolName".uppercase(),
                style = TextStyle(
                    color = Color(0xFF8A8A93),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = arguments,
            style = TextStyle(
                color = Color(0xFF5E5E66),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        )

        // عرض النتيجة بتباين لوني تكتيكي عند توفرها
        if (result != null) {
            Spacer(modifier = Modifier.height(12.dp))
            val (resultColor, resultText) = when (result) {
                is ToolResult.Success -> Color(0xFFD4D4D8) to "SUCCESS :: ${result.output.take(80)}..."
                is ToolResult.Failure -> Color(0xFFD32F2F) to "FAULT :: ${result.reason}"
            }
            Text(
                text = resultText,
                style = TextStyle(
                    color = resultColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
        }
    }
}
