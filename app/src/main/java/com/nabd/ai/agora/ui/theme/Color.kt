package com.nabd.ai.agora.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.materialkolor.scheme.DynamicScheme
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeNeutral
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant
import com.materialkolor.hct.Hct
import com.materialkolor.dynamiccolor.MaterialDynamicColors

enum class SchemeStyle { TONAL_SPOT, EXPRESSIVE, VIBRANT, NEUTRAL }

enum class ColorSchemePreset { MIDNIGHT, NORDIC, FOREST, SUNSET, ROSE, LAVENDER, SLATE, OCEAN }

private val seedColors = mapOf(
    ColorSchemePreset.MIDNIGHT to 0xFF1A237E,
    ColorSchemePreset.NORDIC   to 0xFF546E7A,
    ColorSchemePreset.FOREST   to 0xFF2E7D32,
    ColorSchemePreset.SUNSET   to 0xFFE65100,
    ColorSchemePreset.ROSE     to 0xFFAD1457,
    ColorSchemePreset.LAVENDER to 0xFF7B1FA2,
    ColorSchemePreset.SLATE    to 0xFF455A64,
    ColorSchemePreset.OCEAN    to 0xFF0277BD,
)

fun colorSchemeForPreset(
    preset: ColorSchemePreset,
    style: SchemeStyle = SchemeStyle.TONAL_SPOT,
    isDark: Boolean = false
): ColorScheme {
    val seedArgb = seedColors[preset]!!.toInt()
    val hct = Hct.fromInt(seedArgb)
    val scheme: DynamicScheme = when (style) {
        SchemeStyle.TONAL_SPOT -> SchemeTonalSpot(hct, isDark, 0.0)
        SchemeStyle.EXPRESSIVE -> SchemeExpressive(hct, isDark, 0.0)
        SchemeStyle.VIBRANT   -> SchemeVibrant(hct, isDark, 0.0)
        SchemeStyle.NEUTRAL   -> SchemeNeutral(hct, isDark, 0.0)
    }
    return scheme.toColorScheme()
}

private fun DynamicScheme.toColorScheme(): ColorScheme {
    val mdc = MaterialDynamicColors()
    val c = { color: com.materialkolor.dynamiccolor.DynamicColor -> Color(color.getArgb(this)) }
    return if (isDark) darkColorScheme(
        primary = c(mdc.primary()), onPrimary = c(mdc.onPrimary()),
        primaryContainer = c(mdc.primaryContainer()), onPrimaryContainer = c(mdc.onPrimaryContainer()),
        secondary = c(mdc.secondary()), onSecondary = c(mdc.onSecondary()),
        secondaryContainer = c(mdc.secondaryContainer()), onSecondaryContainer = c(mdc.onSecondaryContainer()),
        tertiary = c(mdc.tertiary()), onTertiary = c(mdc.onTertiary()),
        tertiaryContainer = c(mdc.tertiaryContainer()), onTertiaryContainer = c(mdc.onTertiaryContainer()),
        error = c(mdc.error()), onError = c(mdc.onError()),
        errorContainer = c(mdc.errorContainer()), onErrorContainer = c(mdc.onErrorContainer()),
        background = c(mdc.background()), onBackground = c(mdc.onBackground()),
        surface = c(mdc.surface()), onSurface = c(mdc.onSurface()),
        surfaceVariant = c(mdc.surfaceVariant()), onSurfaceVariant = c(mdc.onSurfaceVariant()),
        outline = c(mdc.outline()), outlineVariant = c(mdc.outlineVariant()),
    ) else lightColorScheme(
        primary = c(mdc.primary()), onPrimary = c(mdc.onPrimary()),
        primaryContainer = c(mdc.primaryContainer()), onPrimaryContainer = c(mdc.onPrimaryContainer()),
        secondary = c(mdc.secondary()), onSecondary = c(mdc.onSecondary()),
        secondaryContainer = c(mdc.secondaryContainer()), onSecondaryContainer = c(mdc.onSecondaryContainer()),
        tertiary = c(mdc.tertiary()), onTertiary = c(mdc.onTertiary()),
        tertiaryContainer = c(mdc.tertiaryContainer()), onTertiaryContainer = c(mdc.onTertiaryContainer()),
        error = c(mdc.error()), onError = c(mdc.onError()),
        errorContainer = c(mdc.errorContainer()), onErrorContainer = c(mdc.onErrorContainer()),
        background = c(mdc.background()), onBackground = c(mdc.onBackground()),
        surface = c(mdc.surface()), onSurface = c(mdc.onSurface()),
        surfaceVariant = c(mdc.surfaceVariant()), onSurfaceVariant = c(mdc.onSurfaceVariant()),
        outline = c(mdc.outline()), outlineVariant = c(mdc.outlineVariant()),
    )
}
