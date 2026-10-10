package ru.kolkhoz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Карточка текущего игрока (DESIGN_SYSTEM.md, раздел 9.2).
 *
 * Блок «корона / ХОДИТ / ИМЯ / СЧЁТ / СЕРИЯ»: корона
 * [KolkhozIcons.Crown] показывается ТОЛЬКО если текущий игрок —
 * единственный лидер партии (см. [isLeader]); статус — Caption,
 * имя — [KolhozTypography.PlayerName], счёт — [ScoreText]
 * (Display), серия — [SeriesIndicator] (только если > 0).
 * Фон прозрачный. Stateless.
 *
 * @param playerName Имя текущего игрока.
 * @param score Счёт текущего игрока.
 * @param series Текущая серия (0 — индикатор не показывается).
 * @param isLeader Текущий игрок — единственный лидер партии
 *   (максимальный счёт > 0); показывает корону. Иначе корона
 *   скрыта. Правило то же, что в [PlayerRow].
 * @param modifier Модификатор.
 */
@Composable
fun CurrentPlayerCard(
    playerName: String,
    score: Int,
    series: Int,
    modifier: Modifier = Modifier,
    isLeader: Boolean = false,
) {
    Column(
        modifier = modifier.padding(vertical = KolhozSpacing.M),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KolhozSpacing.XS),
    ) {
        if (isLeader) {
            Image(
                painter = KolkhozIcons.Crown,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
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
    KolkhozTheme {
        CurrentPlayerCard(
            playerName = "САША",
            score = 7,
            series = 4,
            isLeader = true,
        )
    }
}
