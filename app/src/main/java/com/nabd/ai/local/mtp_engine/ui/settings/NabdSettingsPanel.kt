package com.nabd.ai.local.mtp_engine.ui.settings

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
import com.nabd.ai.local.mtp_engine.domain.generation.InferenceConfig

/**
 * NabdSettingsPanel: Tactical Settings Panel.
 * Intentional Minimalism - Flat panel integrated into the background without containment borders.
 */
@Composable
fun NabdSettingsPanel(
    config: InferenceConfig,
    onConfigUpdate: (InferenceConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    // التكوين المكاني: لوحة مسطحة مدمجة في الخلفية بدون حواف احتواء (Containment)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C))
            .padding(24.dp)
    ) {
        Text(
            text = "SYSTEM :: INFERENCE_CONFIG",
            style = TextStyle(
                color = Color(0xFFE2E2E2),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        NabdNuclearPromptEditor(
            currentPrompt = config.systemPrompt,
            onPromptChange = { onConfigUpdate(config.copy(systemPrompt = it)) },
            modifier = Modifier.padding(bottom = 24.dp)
        )

        NabdInferenceSlider(
            label = "temperature",
            value = config.temperature,
            valueRange = 0.0f..2.0f,
            onValueChange = { onConfigUpdate(config.copy(temperature = it)) }
        )

        NabdInferenceSlider(
            label = "top_p",
            value = config.topP,
            valueRange = 0.0f..1.0f,
            onValueChange = { onConfigUpdate(config.copy(topP = it)) }
        )

        NabdInferenceSlider(
            label = "min_p",
            value = config.minP,
            valueRange = 0.0f..1.0f,
            onValueChange = { onConfigUpdate(config.copy(minP = it)) }
        )
    }
}
