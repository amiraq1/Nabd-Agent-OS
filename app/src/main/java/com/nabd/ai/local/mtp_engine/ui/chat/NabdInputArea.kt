package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.mtp_engine.architecture.NabdAction

/**
 * NabdInputArea: Tactical input area component.
 * Intentional Minimalism - Typography and negative space over complex borders.
 */
@Composable
fun NabdInputArea(
    onAction: (NabdAction) -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    // التكوين المكاني: مساحة سلبية واسعة، لا حدود مرئية، خلفية متماهية بصرياً
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C)) 
            .padding(horizontal = 32.dp, vertical = 24.dp), // كسر التباعد القياسي
        verticalAlignment = Alignment.Bottom
    ) {
        BasicTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            textStyle = TextStyle(
                color = Color(0xFFE2E2E2),
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace, // طباعة تكتيكية خالية من الزخرفة
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(Color(0xFF5E5E66)),
            decorationBox = { innerTextField ->
                if (inputText.isEmpty()) {
                    Text(
                        text = "Awaiting directive...",
                        color = Color(0xFF3A3A40),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                innerTextField()
            },
            enabled = !isGenerating
        )

        // الحركة: تأثيرات انتقال دقيقة وغير مشتتة
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
                modifier = Modifier.padding(start = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Dispatch",
                    tint = Color(0xFFE2E2E2)
                )
            }
        }
    }
}
