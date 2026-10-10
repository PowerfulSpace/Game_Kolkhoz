package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Круглый бейдж с порядковым номером (макеты 02, 03, 05).
 *
 * Круг 32dp: фон [KolhozColors.PrimarySoft], при выделении
 * ([isHighlighted]) — [KolhozColors.Primary]; рамка 1dp
 * соответствующего зелёного. Внутри — номер (Caption Bold).
 * Используется в [PlayerInput], [PlayerRow], итогах партии.
 * Stateless.
 *
 * @param number Порядковый номер (место) для показа.
 * @param isHighlighted Выделить бейдж (текущий игрок / победитель).
 * @param modifier Модификатор.
 */
@Composable
fun NumberBadge(
    number: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
) {
    val background = if (isHighlighted) {
        KolhozColors.Primary
    } else {
        KolhozColors.PrimarySoft
    }
    val borderColor = if (isHighlighted) {
        KolhozColors.Primary
    } else {
        KolhozColors.PrimarySoft
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(background)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = KolhozTypography.Caption.copy(fontWeight = FontWeight.Bold),
            color = KolhozColors.TextPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun NumberBadgePreview() {
    KolkhozTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NumberBadge(number = 1, isHighlighted = true)
            NumberBadge(number = 2)
            NumberBadge(number = 8)
        }
    }
}
