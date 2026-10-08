package ru.kolkhoz.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Стили текста «Колхоз» (DESIGN_SYSTEM.md, раздел 3).
 *
 * Шрифт — системный Roboto ([FontFamily.Default]).
 * Счёт всегда визуально выделен: `Display` 52sp Bold.
 */
object KolhozTypography {
    val Display = TextStyle(
        fontSize = 52.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    )
    val PlayerName = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    )
    val H1 = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    )
    val H2 = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    )
    val Body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Default,
    )
    val BodyMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default,
    )
    val Caption = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default,
    )
    val Button = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    )
    val ButtonSecondary = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default,
    )
}
