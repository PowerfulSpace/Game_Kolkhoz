package ru.kolkhoz.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.ui.components.KolhozTopBar
import ru.kolkhoz.ui.components.ShotHistoryRow
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Временная модель удара для истории (Phase 3b).
 *
 * Только для отображения. В Phase 4 будет заменена
 * на реальную модель из ViewModel.
 *
 * @param sequenceNumber Порядковый номер удара в партии.
 * @param playerName Имя игрока.
 * @param isPocket Забил ли шар.
 * @param time Уже отформатированное время, например `21:34`.
 */
data class ShotUi(
    val sequenceNumber: Int,
    val playerName: String,
    val isPocket: Boolean,
    val time: String,
)

/**
 * Экран истории партии (UI_SPEC.md, раздел 5).
 *
 * Портрет: список `ShotHistoryRow`, свежие сверху.
 * Ландшафт (5.6): таблица с заголовком колонок.
 * Пустое состояние: «Пока нет ударов».
 * Stateless: данные + callbacks, состояние не хранит.
 *
 * @param events Список ударов (в любом порядке).
 * @param onBack Колбэк «назад».
 * @param modifier Модификатор.
 */
@Composable
fun HistoryScreen(
    events: List<ShotUi>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    // Свежие сверху.
    val sorted = events.sortedByDescending { it.sequenceNumber }

    Column(modifier = modifier.fillMaxSize()) {
        KolhozTopBar(title = "ИСТОРИЯ ИГРЫ", onBackClick = onBack)

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(KolhozSpacing.L),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Пока нет ударов",
                    style = KolhozTypography.Body,
                    color = KolhozColors.TextSecondary,
                )
            }
        } else if (isLandscape) {
            // Таблица: заголовок колонок + строки.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KolhozSpacing.L, vertical = KolhozSpacing.S),
                ) {
                    HistoryTableHeaderCell("№", 0.15f)
                    HistoryTableHeaderCell("ИГРОК", 0.45f)
                    HistoryTableHeaderCell("РЕЗУЛЬТАТ", 0.2f)
                    HistoryTableHeaderCell("ВРЕМЯ", 0.2f)
                }
                HorizontalDivider(color = KolhozColors.Border)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    items(sorted, key = { it.sequenceNumber }) { event ->
                        HistoryTableRow(event)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                items(sorted, key = { it.sequenceNumber }) { event ->
                    ShotHistoryRow(
                        index = event.sequenceNumber,
                        playerName = event.playerName,
                        isPocketed = event.isPocket,
                        time = event.time,
                    )
                }
            }
        }
    }
}

/** Ячейка заголовка таблицы; вызывается только внутри [Row]. */
private fun RowScope.HistoryTableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        style = KolhozTypography.Caption,
        color = KolhozColors.TextSecondary,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun HistoryTableRow(event: ShotUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KolhozSpacing.L, vertical = KolhozSpacing.S),
    ) {
        Text(
            text = event.sequenceNumber.toString(),
            style = KolhozTypography.Caption,
            color = KolhozColors.TextSecondary,
            modifier = Modifier.weight(0.15f),
        )
        Text(
            text = event.playerName,
            style = KolhozTypography.Body,
            color = KolhozColors.TextPrimary,
            modifier = Modifier.weight(0.45f),
        )
        Text(
            text = if (event.isPocket) "ЗАБИЛ" else "ПРОМАХ",
            style = KolhozTypography.Body,
            color = if (event.isPocket) KolhozColors.TextPrimary else KolhozColors.TextSecondary,
            modifier = Modifier.weight(0.2f),
        )
        Text(
            text = event.time,
            style = KolhozTypography.Caption,
            color = KolhozColors.TextSecondary,
            modifier = Modifier.weight(0.2f),
        )
    }
}

private fun mockEvents(): List<ShotUi> = listOf(
    ShotUi(28, "Коля", true, "21:34"),
    ShotUi(27, "Коля", true, "21:32"),
    ShotUi(26, "Коля", true, "21:31"),
    ShotUi(25, "Петя", false, "21:28"),
    ShotUi(24, "Петя", true, "21:24"),
)

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun HistoryScreenPortraitPreview() {
    KolkhozTheme {
        HistoryScreen(events = mockEvents(), onBack = {})
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF08110F,
    widthDp = 800,
    heightDp = 400,
)
@Composable
private fun HistoryScreenLandscapePreview() {
    KolkhozTheme {
        HistoryScreen(events = mockEvents(), onBack = {})
    }
}
