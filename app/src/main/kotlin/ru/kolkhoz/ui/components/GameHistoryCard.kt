package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/** Игрок в карточке партии: имя и счёт. */
data class PlayerScore(
    val name: String,
    val score: Int,
)

/**
 * Карточка партии в истории игр (DESIGN_SYSTEM.md, раздел 9.6).
 *
 * Радиус [KolhozRadius.M], фон [KolhozColors.Surface], padding
 * [KolhozSpacing.L]. Содержимое: дата, список игроков со счётом,
 * количество ударов. Stateless.
 *
 * @param date Дата партии, текстом (например `12 октября, 21:10`).
 * @param players Игроки со счётом (в порядке партии).
 * @param shotCount Количество ударов в партии.
 * @param modifier Модификатор.
 */
@Composable
fun GameHistoryCard(
    date: String,
    players: List<PlayerScore>,
    shotCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KolhozRadius.M))
            .background(KolhozColors.Surface)
            .padding(KolhozSpacing.L),
        verticalArrangement = Arrangement.spacedBy(KolhozSpacing.S),
    ) {
        Text(
            text = date,
            style = KolhozTypography.Caption,
            color = KolhozColors.TextSecondary,
        )
        players.forEach { player ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = player.name,
                    style = KolhozTypography.Body,
                    color = KolhozColors.TextPrimary,
                )
                ScoreText(score = player.score, style = rowScoreStyle)
            }
        }
        Text(
            text = "Ударов: $shotCount",
            style = KolhozTypography.Caption,
            color = KolhozColors.TextSecondary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun GameHistoryCardPreview() {
    KolhozTheme {
        GameHistoryCard(
            date = "7 октября, 21:10",
            players = listOf(
                PlayerScore("Саша", 12),
                PlayerScore("Коля", 4),
                PlayerScore("Петя", -5),
                PlayerScore("Дима", -11),
            ),
            shotCount = 42,
        )
    }
}
