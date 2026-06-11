package com.nabd.ai.local.mtp_engine.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex

/**
 * RecomposeSafeMarkdown: Double-buffered crossfade Markdown component.
 * Prevents "flashing" during rapid recompositions in streaming.
 */
@Composable
fun RecomposeSafeMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = true
) {
    var buf0 by remember { mutableStateOf(content) }
    var buf1 by remember { mutableStateOf("") }
    var front by remember { mutableStateOf(0) }
    var fading by remember { mutableStateOf(false) }
    var fadeAlpha by remember { mutableFloatStateOf(0f) }
    var fadeKey by remember { mutableIntStateOf(0) }

    if (fading) {
        // Current buffer is the one that's "front"
    } else {
        val cur = if (front == 0) buf0 else buf1
        if (content != cur) {
            if (front == 0) buf1 = content else buf0 = content
            fadeKey++
            fading = true
            fadeAlpha = 0f
        }
    }

    LaunchedEffect(fadeKey) {
        if (!fading) return@LaunchedEffect
        val startNs = withFrameNanos { it }
        val durationNs = 180_000_000L
        while (true) {
            val nowNs = withFrameNanos { it }
            val p = ((nowNs - startNs).toFloat() / durationNs).coerceAtMost(1f)
            fadeAlpha = p
            if (p >= 1f) break
        }
        front = 1 - front
        fading = false
        fadeAlpha = 0f
    }

    val incoming = 1 - front
    val z0 = when { fading && incoming == 0 -> 2f; front == 0 -> 1f; else -> 0f }
    val a0 = when { fading && incoming == 0 -> fadeAlpha; front == 0 -> 1f; else -> 0f }
    val z1 = when { fading && incoming == 1 -> 2f; front == 1 -> 1f; else -> 0f }
    val a1 = when { fading && incoming == 1 -> fadeAlpha; front == 1 -> 1f; else -> 0f }

    Box(modifier = modifier) {
        if (buf0.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().zIndex(z0).alpha(a0)) {
                androidx.compose.material3.Text(
                    text = buf0,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (buf1.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().zIndex(z1).alpha(a1)) {
                androidx.compose.material3.Text(
                    text = buf1,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
