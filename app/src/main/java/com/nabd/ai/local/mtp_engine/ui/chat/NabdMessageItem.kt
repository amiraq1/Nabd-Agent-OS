package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant
import com.nabd.ai.local.mtp_engine.architecture.MessageStatus
import com.nabd.ai.local.mtp_engine.ui.components.RecomposeSafeMarkdown

@Composable
fun NabdMessageItem(
    message: ChatMessage,
    onEditClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // تحديد الهوية البصرية التكتيكية
    val isUser = message.participant == Participant.USER
    val tacticalShape = RoundedCornerShape(2.dp)
    
    // Workaround for surfaceContainerLowest which might be missing in older Material 3 versions
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    }

    val borderColor = if (isUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // شريط الحالة العلوي (يظهر للمودل فقط)
        if (!isUser) {
            AgentStatusHeader(status = message.status, modelName = message.modelName)
        }

        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(tacticalShape)
                .background(containerColor)
                .border(1.dp, borderColor, tacticalShape)
                .padding(12.dp)
        ) {
            Column {
                // عرض كتل التفكير أو الأدوات إن وجدت (ToolCalls / Thoughts)
                if (!message.thoughts.isNullOrEmpty()) {
                    TacticalThoughtBlock(
                        title = message.thoughtTitle ?: "ANALYSIS",
                        content = message.thoughts
                    )
                }

                // عرض محتوى الرسالة الأساسي باستخدام RecomposeSafeMarkdown
                if (isUser) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    RecomposeSafeMarkdown(
                        content = message.text,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // عرض المرفقات إن وجدت أسفل النص
                if (message.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TacticalAttachmentGrid(attachments = message.attachments)
                }
            }
        }
    }
}

@Composable
fun AgentStatusHeader(status: MessageStatus, modelName: String?) {
    val statusText = when (status) {
        MessageStatus.SENDING -> "INITIALIZING..."
        MessageStatus.THINKING -> "COMPUTING TACTICS..."
        MessageStatus.TOOL_CALLING -> "EXECUTING PROTOCOL..."
        MessageStatus.TRANSCRIBING -> "DECODING MEDIA..."
        MessageStatus.ERROR -> "SYSTEM FAILURE"
        else -> "AGENT_OS : ${modelName?.uppercase() ?: "LOCAL_INFERENCE"}"
    }
    
    val statusColor = if (status == MessageStatus.ERROR) Color.Red else MaterialTheme.colorScheme.primary

    Text(
        text = "> $statusText",
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = statusColor,
        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
    )
}

@Composable
fun TacticalThoughtBlock(title: String, content: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        // شريط العنوان القابل للنقر
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[$title]",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // المحتوى المنسدل
        AnimatedVisibility(visible = expanded) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
            )
        }
    }
}

// دالة مبسطة لعرض شبكة المرفقات
@Composable
fun TacticalAttachmentGrid(attachments: List<com.nabd.ai.local.mtp_engine.architecture.SelectedAttachment>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        attachments.forEach { attachment ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = attachment.type.take(3).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
