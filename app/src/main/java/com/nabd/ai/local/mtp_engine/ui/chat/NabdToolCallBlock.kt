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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F12))
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "EXEC :: $toolName".uppercase(),
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF6B6B76),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = arguments,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF6B6B76),
                fontFamily = FontFamily.Monospace
            )
        )

        if (result != null) {
            Spacer(modifier = Modifier.height(12.dp))
            val (resultColor, resultText) = when (result) {
                is ToolResult.Success -> Color(0xFF00E5FF) to "SUCCESS :: ${result.output.take(80)}..."
                is ToolResult.Failure -> androidx.compose.material3.MaterialTheme.colorScheme.primary to "FAULT :: ${result.reason}"
            }
            Text(
                text = resultText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    color = resultColor,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}
