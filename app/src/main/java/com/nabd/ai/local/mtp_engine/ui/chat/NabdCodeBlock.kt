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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F12)) // Tactical Code Background
            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 16.dp)
    ) {
        if (!language.isNullOrBlank()) {
            Text(
                text = language.uppercase(),
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF6B6B76), // SteelGray
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Text(
            text = code,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF00E5FF), // Cyan code for tactical terminal feel
                fontFamily = FontFamily.Monospace,
                lineHeight = 24.sp
            )
        )
    }
}
