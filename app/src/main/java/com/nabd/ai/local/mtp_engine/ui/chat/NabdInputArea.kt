package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.mtp_engine.architecture.NabdAction

@Composable
fun NabdInputArea(
    onAction: (NabdAction) -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // The Signature Element: The Heartbeat Pulse
        // Active when generating
        AnimatedVisibility(visible = isGenerating) {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = if (isGenerating) "Processing..." else "Enter directive...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                },
                enabled = !isGenerating
            )

            AnimatedVisibility(
                visible = inputText.isNotBlank() && !isGenerating,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                IconButton(
                    onClick = {
                        onAction(NabdAction.ProcessPrompt(inputText))
                        inputText = ""
                    },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Execute",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
