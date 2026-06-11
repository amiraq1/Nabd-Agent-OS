package com.nabd.ai.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class SchemeStyle { TONAL_SPOT, EXPRESSIVE, VIBRANT, NEUTRAL }

enum class ColorSchemePreset { MIDNIGHT, NORDIC, FOREST, SUNSET, ROSE, LAVENDER, SLATE, OCEAN }

/**
 * Fallback static schemes for Nabd-Agent-OS.
 * Replaces materialkolor dynamic generation for compatibility.
 */
fun colorSchemeForPreset(
    preset: ColorSchemePreset,
    style: SchemeStyle = SchemeStyle.TONAL_SPOT,
    isDark: Boolean = false
): ColorScheme {
    // Basic implementation to satisfy the Theme.kt requirements without the external library
    return if (isDark) {
        when (preset) {
            ColorSchemePreset.MIDNIGHT -> darkColorScheme(
                primary = Color(0xFFBAC3FF),
                onPrimary = Color(0xFF001061),
                primaryContainer = Color(0xFF001D91),
                onPrimaryContainer = Color(0xFFDEE0FF),
                background = Color(0xFF1B1B1F),
                surface = Color(0xFF1B1B1F)
            )
            ColorSchemePreset.NORDIC -> darkColorScheme(
                primary = Color(0xFF82CFFF),
                onPrimary = Color(0xFF00344B),
                primaryContainer = Color(0xFF004C6B),
                onPrimaryContainer = Color(0xFFC7E7FF),
                background = Color(0xFF191C1E),
                surface = Color(0xFF191C1E)
            )
            else -> darkColorScheme() // Default fallback
        }
    } else {
        when (preset) {
            ColorSchemePreset.MIDNIGHT -> lightColorScheme(
                primary = Color(0xFF3F51B5),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDEE0FF),
                onPrimaryContainer = Color(0xFF001061),
                background = Color(0xFFFEFBFF),
                surface = Color(0xFFFEFBFF)
            )
            ColorSchemePreset.NORDIC -> lightColorScheme(
                primary = Color(0xFF00648D),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFC7E7FF),
                onPrimaryContainer = Color(0xFF001E2E),
                background = Color(0xFFFCFCFF),
                surface = Color(0xFFFCFCFF)
            )
            else -> lightColorScheme() // Default fallback
        }
    }
}
