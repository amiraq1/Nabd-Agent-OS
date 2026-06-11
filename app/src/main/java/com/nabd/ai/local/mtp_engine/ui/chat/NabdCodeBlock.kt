package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * NabdCodeBlock: Borderless Monochromatic Code Block.
 * Intentional Minimalism - Typography and subtle contrast over containers.
 */
@Composable
fun NabdCodeBlock(
    code: String,
    language: String? = null,
    modifier: Modifier = Modifier
) {
    // التكوين المكاني: كسر الشبكة عبر حواف حادة وتباين لوني طفيف جداً بدلاً من البطاقات (Cards)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF121214)) // تباين دقيق عن الخلفية 0xFF0A0A0C
            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 16.dp)
    ) {
        if (!language.isNullOrBlank()) {
            Text(
                text = language.uppercase(),
                style = TextStyle(
                    color = Color(0xFF5E5E66),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Text(
            text = code,
            style = TextStyle(
                color = Color(0xFFD4D4D8),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace, // طباعة برمجية صرفة
                lineHeight = 24.sp
            )
        )
    }
}
