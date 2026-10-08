package ru.kolkhoz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Material3-схема на токенах [KolhozColors] и [KolhozTypography].
 *
 * Приложение всегда тёмное (DESIGN_SYSTEM.md, раздел 1):
 * светлой темы нет.
 */
private val KolkhozColorScheme = darkColorScheme(
    background = KolhozColors.Background,
    onBackground = KolhozColors.TextPrimary,
    surface = KolhozColors.Surface,
    onSurface = KolhozColors.TextPrimary,
    surfaceVariant = KolhozColors.SurfaceElevated,
    onSurfaceVariant = KolhozColors.TextSecondary,
    primary = KolhozColors.Primary,
    onPrimary = KolhozColors.TextPrimary,
    primaryContainer = KolhozColors.PrimaryDark,
    onPrimaryContainer = KolhozColors.TextPrimary,
    secondary = KolhozColors.PrimarySoft,
    onSecondary = KolhozColors.TextPrimary,
    error = KolhozColors.Negative,
    onError = KolhozColors.TextPrimary,
    outline = KolhozColors.Border,
    outlineVariant = KolhozColors.Border,
)

/** Маппинг токенов [KolhozTypography] на стили Material3. */
private val KolkhozMaterialTypography = Typography(
    displayLarge = KolhozTypography.Display,
    headlineLarge = KolhozTypography.PlayerName,
    headlineMedium = KolhozTypography.H1,
    headlineSmall = KolhozTypography.H2,
    titleLarge = KolhozTypography.Button,
    bodyLarge = KolhozTypography.Body,
    bodyMedium = KolhozTypography.BodyMedium,
    labelLarge = KolhozTypography.ButtonSecondary,
    labelMedium = KolhozTypography.Caption,
)

/**
 * Корневая тема приложения «Колхоз».
 *
 * Компоненты берут цвета и размеры напрямую из токенов
 * ([KolhozColors], [KolhozTypography], [KolhozSpacing],
 * [KolhozRadius]); схема ниже нужна Material3-виджетам.
 *
 * @param content Контент приложения.
 */
@Composable
fun KolkhozTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KolkhozColorScheme,
        typography = KolkhozMaterialTypography,
        content = content,
    )
}
