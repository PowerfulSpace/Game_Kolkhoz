package ru.kolkhoz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Вторичная кнопка, например ПРОМАХ (DESIGN_SYSTEM.md, раздел 8.2).
 *
 * Высота 56dp, прозрачный фон, граница 1dp [KolhozColors.Border].
 * Pressed — фон [KolhozColors.Surface]. Stateless.
 *
 * @param text Текст кнопки.
 * @param onClick Колбэк нажатия.
 * @param enabled Активна ли кнопка.
 * @param leadingIcon Необязательная иконка слева от текста
 *   (векторная, tint = цвет текста).
 * @param leadingPainter Необязательная растровая иконка (PNG)
 *   слева от текста; рисуется как есть, БЕЗ tint (иначе
 *   PNG закрашивается целиком). Если переданы оба параметра —
 *   приоритет у [leadingPainter].
 * @param modifier Модификатор.
 */
@Composable
fun KolhozSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingPainter: Painter? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val background = when {
        pressed && enabled -> KolhozColors.Surface
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .height(56.dp)
            .widthIn(min = 200.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(KolhozRadius.L))
            .background(background)
            .border(
                width = 1.dp,
                color = KolhozColors.Border,
                shape = RoundedCornerShape(KolhozRadius.L),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = KolhozSpacing.XXL),
        contentAlignment = Alignment.Center,
    ) {
        // Текст — по центру кнопки.
        Text(
            text = text,
            style = KolhozTypography.ButtonSecondary,
            color = KolhozColors.TextPrimary,
        )
        // Иконка — слева от центра, если есть.
        // PNG (leadingPainter) — без tint, иначе PNG закрасится
        // целиком; вектор (leadingIcon) — с tint цвета текста.
        when {
            leadingPainter != null -> Image(
                painter = leadingPainter,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp),
            )
            leadingIcon != null -> Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = KolhozColors.TextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun KolhozSecondaryButtonPreview() {
    KolkhozTheme {
        Column(
            modifier = Modifier.padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.M),
        ) {
            KolhozSecondaryButton(text = "ПРОМАХ", onClick = {})
            KolhozSecondaryButton(text = "НА ГЛАВНУЮ", onClick = {}, enabled = false)
        }
    }
}
