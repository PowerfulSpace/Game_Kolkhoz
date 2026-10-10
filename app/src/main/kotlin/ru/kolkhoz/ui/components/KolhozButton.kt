package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
 * Главная кнопка (DESIGN_SYSTEM.md, раздел 8.1).
 *
 * Высота 64dp, радиус [KolhozRadius.L], фон [KolhozColors.Primary].
 * Pressed — [KolhozColors.PrimaryDark]. Disabled — opacity 40%,
 * текст [KolhozColors.TextDisabled]. Stateless.
 *
 * @param text Текст кнопки.
 * @param onClick Колбэк нажатия.
 * @param enabled Активна ли кнопка.
 * @param leadingIcon Необязательная иконка слева от текста.
 * @param modifier Модификатор.
 */
@Composable
fun KolhozButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val background = when {
        !enabled -> KolhozColors.Primary.copy(alpha = 0.4f)
        pressed -> KolhozColors.PrimaryDark
        else -> KolhozColors.Primary
    }
    val textColor = if (enabled) {
        KolhozColors.TextPrimary
    } else {
        KolhozColors.TextDisabled
    }

    Box(
        modifier = modifier
            .height(64.dp)
            .widthIn(min = 200.dp)
            .clip(RoundedCornerShape(KolhozRadius.L))
            .background(background)
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
            style = KolhozTypography.Button,
            color = textColor,
        )
        // Иконка — слева от центра, если есть.
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun KolhozButtonPreview() {
    KolkhozTheme {
        Column(
            modifier = Modifier.padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.M),
        ) {
            KolhozButton(text = "НОВАЯ ИГРА", onClick = {})
            KolhozButton(text = "НАЧАТЬ ИГРУ", onClick = {}, enabled = false)
        }
    }
}
