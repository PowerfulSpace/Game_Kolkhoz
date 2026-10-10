package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Нижняя панель игрового экрана (DESIGN_SYSTEM.md, раздел 9.9).
 *
 * Высота 56dp: `↶ ОТМЕНА` и `ИСТОРИЯ` — обе текстовые
 * кнопки со слотами для callbacks, разделены вертикальной
 * линией, панель в рамке 1dp. Отмена может быть disabled
 * (UI_SPEC.md, раздел 4.5: при пустой истории). Stateless.
 *
 * @param onUndoClick Колбэк «Отмена удара».
 * @param onHistoryClick Колбэк «История».
 * @param undoEnabled Активна ли кнопка отмены.
 * @param modifier Модификатор.
 */
@Composable
fun KolhozBottomBar(
    onUndoClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    undoEnabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(KolhozColors.Background)
            .border(
                width = 1.dp,
                color = KolhozColors.Border,
                shape = RoundedCornerShape(KolhozRadius.M),
            )
            .padding(horizontal = KolhozSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomBarAction(
            icon = rememberVectorPainter(KolkhozIcons.Undo),
            label = "ОТМЕНА",
            enabled = undoEnabled,
            onClick = onUndoClick,
            modifier = Modifier.weight(1f),
        )
        // Вертикальный разделитель между двумя действиями.
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(KolhozColors.Border.copy(alpha = 0.4f)),
        )
        BottomBarAction(
            icon = KolkhozIcons.History,
            label = "ИСТОРИЯ",
            enabled = true,
            onClick = onHistoryClick,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Элемент нижней панели: иконка + текст, стиль text button. */
@Composable
private fun BottomBarAction(
    icon: Painter,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (enabled) {
        KolhozColors.TextPrimary
    } else {
        KolhozColors.TextDisabled
    }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = KolhozSpacing.M, vertical = KolhozSpacing.S),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.size(KolhozSpacing.S))
        Text(
            text = label,
            style = KolhozTypography.BodyMedium,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun KolhozBottomBarPreview() {
    KolkhozTheme {
        KolhozBottomBar(onUndoClick = {}, onHistoryClick = {})
    }
}
