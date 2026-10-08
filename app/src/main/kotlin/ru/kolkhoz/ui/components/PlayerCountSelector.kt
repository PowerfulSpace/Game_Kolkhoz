package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalIndication
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Счётчик количества игроков (DESIGN_SYSTEM.md, раздел 9.11).
 *
 * Строка `− | N | +`. Кнопки 56×56dp, граница 1dp
 * [KolhozColors.Border], disabled при достижении границы
 * (opacity 40%). N — [KolhozTypography.H1]. Stateless.
 *
 * @param value Текущее количество игроков.
 * @param onDecrease Колбэк уменьшения (при [value] > [min]).
 * @param onIncrease Колбэк увеличения (при [value] < [max]).
 * @param min Минимум игроков.
 * @param max Максимум игроков.
 * @param modifier Модификатор.
 */
@Composable
fun PlayerCountSelector(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 3,
    max: Int = 8,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KolhozSpacing.XL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SelectorButton(
            icon = KolkhozIcons.Decrease,
            contentDescription = "Убавить",
            enabled = value > min,
            onClick = onDecrease,
        )
        Text(
            text = value.toString(),
            style = KolhozTypography.H1,
            color = KolhozColors.TextPrimary,
        )
        SelectorButton(
            icon = KolkhozIcons.Increase,
            contentDescription = "Прибавить",
            enabled = value < max,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun SelectorButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(56.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(KolhozRadius.L))
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = KolhozColors.Border,
                shape = RoundedCornerShape(KolhozRadius.L),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = KolhozColors.TextPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun PlayerCountSelectorPreview() {
    KolhozTheme {
        PlayerCountSelector(value = 4, onDecrease = {}, onIncrease = {})
    }
}
