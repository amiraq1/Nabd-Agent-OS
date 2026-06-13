package com.nabd.ai.local.mtp_engine.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.mtp_engine.domain.generation.GenerationError

/**
 * NabdErrorBanner: Brutalist Error State component.
 * Intentional Minimalism - Tactical error reporting.
 */
@Composable
fun NabdErrorBanner(
    error: GenerationError,
    modifier: Modifier = Modifier
) {
    // التكوين المكاني: لا نستخدم الألوان الحمراء الفاقعة بشكل مبتذل.
    // نعتمد على خلفية داكنة سياقية مع طباعة أحادية المسافة صارمة.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.errorContainer) // أحمر داكن جداً يكسر اللون الأحادي بهدوء
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SYSTEM_FAULT :: ${extractErrorMessage(error).uppercase()}",
            style = TextStyle(
                color = androidx.compose.material3.MaterialTheme.colorScheme.error, // أحمر مطفأ (Muted Red)
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            )
        )
    }
}

private fun extractErrorMessage(error: GenerationError): String {
    return when (error) {
        is GenerationError.EngineFault -> error.message
        is GenerationError.ModelError -> error.message
        is GenerationError.ContextFull -> "CONTEXT_WINDOW_EXCEEDED: ${error.message}"
        is GenerationError.Unknown -> error.message
    }
}
