package ru.kolkhoz.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.components.KolhozTopBar
import ru.kolkhoz.ui.components.KolkhozIcons
import ru.kolkhoz.ui.components.ShotHistoryRow
import ru.kolkhoz.ui.model.HistoryUiState
import ru.kolkhoz.ui.model.ShotUi
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Экран истории партии (UI_SPEC.md, раздел 5).
 *
 * Портрет: список `ShotHistoryRow`, свежие сверху.
 * Ландшафт (5.6): таблица с заголовком колонок.
 * Пустое состояние: «Пока нет ударов».
 * Stateless: принимает UI-state целиком и callback.
 *
 * @param uiState Состояние истории (уже отсортировано свежими вперёд).
 * @param onBack Колбэк «назад».
 * @param modifier Модификатор.
 */
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(modifier = modifier.fillMaxSize()) {
        KolhozTopBar(title = "ИСТОРИЯ ИГРЫ", onBackClick = onBack)

        if (uiState.isEmpty) {
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
                    items(uiState.events, key = { it.sequenceNumber }) { event ->
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
                items(uiState.events, key = { it.sequenceNumber }) { event ->
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
@Composable
private fun RowScope.HistoryTableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        style = KolhozTypography.Caption,
        color = KolhozColors.TextSecondary,
        modifier = Modifier.weight(weight),
    )
}

/** Строка таблицы истории в landscape (UI_SPEC.md, 5.6). */
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.2f),
        ) {
            Icon(
                imageVector = if (event.isPocket) {
                    KolkhozIcons.Pocket
                } else {
                    KolkhozIcons.Miss
                },
                contentDescription = null,
                tint = if (event.isPocket) {
                    KolhozColors.Positive
                } else {
                    KolhozColors.Negative
                },
                modifier = Modifier.size(if (event.isPocket) 8.dp else 12.dp),
            )
            Spacer(Modifier.width(KolhozSpacing.XS))
            Text(
                text = if (event.isPocket) "ЗАБИЛ" else "ПРОМАХ",
                style = KolhozTypography.Body,
                color = if (event.isPocket) {
                    KolhozColors.Positive
                } else {
                    KolhozColors.Negative
                },
            )
        }
        Text(
            text = event.time,
            style = KolhozTypography.Caption,
            color = KolhozColors.TextSecondary,
            modifier = Modifier.weight(0.2f),
        )
    }
}

private fun previewState(): HistoryUiState = HistoryUiState(
    events = listOf(
        ShotUi(28, "Коля", true, "21:34"),
        ShotUi(27, "Коля", true, "21:32"),
        ShotUi(26, "Коля", true, "21:31"),
        ShotUi(25, "Петя", false, "21:28"),
        ShotUi(24, "Петя", true, "21:24"),
    ),
    isEmpty = false,
)

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun HistoryScreenPortraitPreview() {
    KolkhozTheme {
        HistoryScreen(uiState = previewState(), onBack = {})
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
        HistoryScreen(uiState = previewState(), onBack = {})
    }
}
