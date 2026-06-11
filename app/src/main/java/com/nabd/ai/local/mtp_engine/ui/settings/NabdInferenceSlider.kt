package com.nabd.ai.local.mtp_engine.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * NabdInferenceSlider: Tactical Inference Controls.
 * Intentional Minimalism - Strict negative space, pure linear composition.
 */
@Composable
fun NabdInferenceSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // التكوين المكاني: مساحة سلبية صارمة وتكوين خطي نقي خالي من البطاقات (Cards)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    color = Color(0xFF8A8A93), // لون باهت للتسلسل الهرمي
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = String.format("%.2f", value),
                style = TextStyle(
                    color = Color(0xFFD4D4D8), // لون ساطع للبيانات النشطة
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // تخصيص الألوان لكسر المظهر الافتراضي لـ Material 3
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE2E2E2),
                activeTrackColor = Color(0xFF5E5E66),
                inactiveTrackColor = Color(0xFF1A1A1E)
            )
        )
    }
}
