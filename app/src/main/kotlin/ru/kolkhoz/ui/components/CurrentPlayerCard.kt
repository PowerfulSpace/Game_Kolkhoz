package ru.kolkhoz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Карточка текущего игрока (DESIGN_SYSTEM.md, раздел 9.2).
 *
 * Блок «ХОДИТ / ИМЯ / СЧЁТ / СЕРИЯ»: статус — Caption,
 * имя — [KolhozTypography.PlayerName], счёт — [ScoreText]
 * (Display), серия — [SeriesIndicator] (только если > 0).
 * Фон прозрачный. Stateless.
 *
 * @param playerName Имя текущего игрока.
 * @param score Счёт текущего игрока.
 * @param series Текущая серия (0 — индикатор не показывается).
 * @param modifier Модификатор.
 */
@Composable
fun CurrentPlayerCard(
    playerName: String,
    score: Int,
    series: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = KolhozSpacing.M),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KolhozSpacing.XS),
    ) {
        Text(
            text = "ХОДИТ",
            style = KolhozTypography.Caption,
            color = KolhozColors.TextSecondary,
        )
        Text(
            text = playerName,
            style = KolhozTypography.PlayerName,
            color = KolhozColors.TextPrimary,
        )
        ScoreText(score = score)
        if (series > 0) {
            SeriesIndicator(series = series)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun CurrentPlayerCardPreview() {
    KolhozTheme {
        CurrentPlayerCard(playerName = "САША", score = 7, series = 4)
    }
}
