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
                primary = Color(0xFF38BDF8), // Tactical Sky Blue
                onPrimary = Color(0xFF082F49),
                secondary = Color(0xFF94A3B8), // Slate Secondary
                onSecondary = Color(0xFF0F172A),
                background = Color(0xFF13141A), // Agora Background Deep Black
                onBackground = Color.White,
                surface = Color(0xFF1E1F28), // Agora Surface
                onSurface = Color.White,
                surfaceVariant = Color(0xFF2A2B36), // Agora Input Dark
                onSurfaceVariant = Color(0xFF9EA1B0), // Text Secondary
                outline = Color(0xFF3B3C46), // Accent Color
                inverseSurface = Color(0xFF0F0F12), // Deep Terminal Black
                secondaryContainer = Color(0xFF1A1A1E), // Muted Secondary
                tertiary = Color(0xFF00E5FF), // Tactical Cyan
                error = Color(0xFFD32F2F), // Muted Red for errors
                errorContainer = Color(0xFF1A1212) // Very dark red for error backgrounds
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
