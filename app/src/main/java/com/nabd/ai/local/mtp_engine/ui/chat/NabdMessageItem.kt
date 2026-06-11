package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.mtp_engine.architecture.ChatMessage
import com.nabd.ai.local.mtp_engine.architecture.Participant

/**
 * NabdMessageItem: Minimalist message display component.
 * typography and asymmetry over traditional chat bubbles.
 */
@Composable
fun NabdMessageItem(
    message: ChatMessage,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    val isUser = message.participant == Participant.USER

    // التكوين المكاني: محاذاة تعتمد على المساحة السلبية بدلاً من الحواف القاسية
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 64.dp else 16.dp,
                end = if (isUser) 16.dp else 64.dp,
                top = 24.dp,
                bottom = 24.dp
            )
            .animateContentSize( // حركة ناعمة للتفاعلات الدقيقة وتدفق النص
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // رفض الجماليات النمطية: لا فقاعات، الاعتماد على التباين اللوني والطباعة
        NabdStreamingText(
            content = message.text,
            isStreaming = isStreaming && !isUser,
            color = if (isUser) Color(0xFFE2E2E2) else Color(0xFFA0A0A5),
            fontSize = if (isUser) 18.sp else 16.sp,
            fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(bottom = if (isStreaming && !isUser) 8.dp else 0.dp)
        )

        // مؤشر تدفق بصري بسيط للغاية يظهر فقط عند الحاجة
        if (isStreaming && !isUser) {
            StreamingIndicator()
        }
    }
}

@Composable
private fun StreamingIndicator() {
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(Color(0xFF5E5E66), shape = CircleShape)
    )
}
