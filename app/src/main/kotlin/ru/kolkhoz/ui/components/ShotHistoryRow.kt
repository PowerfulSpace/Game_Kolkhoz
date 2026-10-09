package ru.kolkhoz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Строка истории ударов (DESIGN_SYSTEM.md, раздел 9.5).
 *
 * Высота 48dp, структура `№ | Игрок | Результат | Время`.
 * Результат: `● ЗАБИЛ` — [KolhozColors.Positive],
 * `× ПРОМАХ` — [KolhozColors.Negative] (иконки, не Unicode).
 * Внизу — тонкий разделитель. Stateless.
 *
 * @param index Номер удара в партии.
 * @param playerName Имя игрока.
 * @param isPocketed Забил ли шар (true) или промахнулся (false).
 * @param time Время удара, текстом (например `21:34`).
 * @param modifier Модификатор.
 */
@Composable
fun ShotHistoryRow(
    index: Int,
    playerName: String,
    isPocketed: Boolean,
    time: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = KolhozSpacing.L),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KolhozSpacing.M),
        ) {
            Text(
                text = index.toString(),
                style = KolhozTypography.Caption,
                color = KolhozColors.TextSecondary,
                modifier = Modifier.widthIn(min = 24.dp),
            )
            Text(
                text = playerName,
                style = KolhozTypography.Body,
                color = KolhozColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (isPocketed) {
                    KolkhozIcons.Pocket
                } else {
                    KolkhozIcons.Miss
                },
                contentDescription = null,
                tint = if (isPocketed) {
                    KolhozColors.Positive
                } else {
                    KolhozColors.Negative
                },
                modifier = Modifier.size(if (isPocketed) 8.dp else 12.dp),
            )
            Text(
                text = if (isPocketed) "ЗАБИЛ" else "ПРОМАХ",
                style = KolhozTypography.BodyMedium,
                color = if (isPocketed) {
                    KolhozColors.Positive
                } else {
                    KolhozColors.Negative
                },
            )
            Text(
                text = time,
                style = KolhozTypography.Caption,
                color = KolhozColors.TextSecondary,
            )
        }
        HorizontalDivider(
            color = KolhozColors.Border.copy(alpha = 0.25f),
            thickness = 1.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun ShotHistoryRowPreview() {
    KolkhozTheme {
        Column {
            ShotHistoryRow(index = 28, playerName = "Коля", isPocketed = true, time = "21:34")
            ShotHistoryRow(index = 25, playerName = "Петя", isPocketed = false, time = "21:28")
        }
    }
}
