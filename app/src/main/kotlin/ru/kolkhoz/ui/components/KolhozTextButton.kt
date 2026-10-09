package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Текстовая кнопка, например ИСТОРИЯ, НАЗАД
 * (DESIGN_SYSTEM.md, раздел 8.3).
 *
 * Без заливки и границы. Высота от 48dp.
 * Pressed — фон [KolhozColors.Surface]. Stateless.
 *
 * @param text Текст кнопки.
 * @param onClick Колбэк нажатия.
 * @param enabled Активна ли кнопка.
 * @param textColor Цвет текста ([KolhozColors.TextPrimary] по
 *   умолчанию; варианты Primary / Negative — для диалогов).
 * @param modifier Модификатор.
 */
@Composable
fun KolhozTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = KolhozColors.TextPrimary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val background = if (pressed && enabled) {
        KolhozColors.Surface
    } else {
        Color.Transparent
    }
    val contentColor = if (enabled) textColor else KolhozColors.TextDisabled

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(KolhozRadius.S))
            .background(background)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = KolhozSpacing.M, vertical = KolhozSpacing.S),
    ) {
        Text(
            text = text,
            style = KolhozTypography.BodyMedium,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun KolhozTextButtonPreview() {
    KolkhozTheme {
        Column(
            modifier = Modifier.padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.S),
        ) {
            KolhozTextButton(text = "ИСТОРИЯ", onClick = {})
            KolhozTextButton(
                text = "ЗАВЕРШИТЬ",
                onClick = {},
                textColor = KolhozColors.Negative,
            )
        }
    }
}
