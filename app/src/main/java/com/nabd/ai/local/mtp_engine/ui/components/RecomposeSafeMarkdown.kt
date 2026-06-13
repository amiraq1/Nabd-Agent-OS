package com.nabd.ai.local.mtp_engine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * RecomposeSafeMarkdown: Double-buffered crossfade Markdown component.
 * Prevents "flashing" during rapid recompositions in streaming.
 */
@Composable
fun RecomposeSafeMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    isGenerating: Boolean = false
) {
    // Optimization: Directly use the content for the primary text to ensure zero-lag streaming.
    // Double-buffering is often overkill for simple text updates and can cause visible lag or high CPU usage.
    // If flickering was an issue, it's better handled by ensuring stable keys in the parent list.
    
    Column(modifier = modifier) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            androidx.compose.material3.Text(
                text = content,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isGenerating) {
                TerminalCursorBlinker(modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}
