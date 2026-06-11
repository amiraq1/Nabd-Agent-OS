package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant

@Composable
fun NabdMessageItem(
    message: ChatMessage,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    val isUser = message.participant == Participant.USER

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        // Hairline Divider for structural separation
        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), thickness = 1.dp)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Left-edge indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(
                        color = if (isUser) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Structural Eyebrow (Participant Label)
                Text(
                    text = if (isUser) "[ USER ]" else "[ NABD ]",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                NabdStreamingText(
                    content = message.text,
                    isStreaming = isStreaming && !isUser,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}
