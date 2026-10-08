package ru.kolkhoz.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Токены цветов «Колхоз» (DESIGN_SYSTEM.md, раздел 2).
 *
 * Правила: `Primary` — действие, `Positive` — состояние счёта.
 * Два зелёных осознанно разные. Красный и жёлтый — только
 * семантически.
 */
object KolhozColors {
    // Background
    val Background = Color(0xFF08110F)
    val Surface = Color(0xFF0D1A17)
    val SurfaceElevated = Color(0xFF12231F)

    // Brand
    val Primary = Color(0xFF18C77A)
    val PrimaryDark = Color(0xFF0D8F59)
    val PrimarySoft = Color(0xFF1A6B4D)

    // Semantic
    val Positive = Color(0xFF32D583)
    val Negative = Color(0xFFF06455)
    val Warning = Color(0xFFF4C95D)
    val Neutral = Color(0xFF8A9A95)

    // Text
    val TextPrimary = Color(0xFFF2F5F3)
    val TextSecondary = Color(0xFFA8B5B0)
    val TextDisabled = Color(0xFF53625D)

    // Border
    val Border = Color(0xFF1A5140)
}
