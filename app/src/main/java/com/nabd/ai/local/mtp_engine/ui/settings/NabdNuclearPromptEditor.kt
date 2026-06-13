package com.nabd.ai.local.mtp_engine.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * NabdNuclearPromptEditor: Brutalist Prompt Editor.
 * Intentional Minimalism - Tactical configuration block.
 */
@Composable
fun NabdNuclearPromptEditor(
    currentPrompt: String,
    onPromptChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // التكوين المكاني: حواف حادة، ألوان تحذيرية مطفأة، وطباعة برمجية فقط
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.inverseSurface)
            .padding(24.dp)
    ) {
        Text(
            text = "SYSTEM :: NUCLEAR_DIRECTIVE",
            style = TextStyle(
                color = androidx.compose.material3.MaterialTheme.colorScheme.error, // أحمر مطفأ دلالة على حساسية التكوين
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BasicTextField(
            value = currentPrompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            textStyle = TextStyle(
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(androidx.compose.material3.MaterialTheme.colorScheme.error)
        )
    }
}
