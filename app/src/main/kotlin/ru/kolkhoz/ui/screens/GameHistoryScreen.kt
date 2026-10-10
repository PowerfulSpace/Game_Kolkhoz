package ru.kolkhoz.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.ui.components.CenteredContent
import ru.kolkhoz.ui.components.GameHistoryCard
import ru.kolkhoz.ui.components.KolhozButton
import ru.kolkhoz.ui.components.KolhozTopBar
import ru.kolkhoz.ui.components.PlayerScore
import ru.kolkhoz.ui.model.GameHistoryItemUi
import ru.kolkhoz.ui.model.GameHistoryUiState
import ru.kolkhoz.ui.model.PlayerResultUi
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Экран «История игр» — список сыгранных партий
 * (DESIGN_SYSTEM.md, раздел 13.1).
 *
 * Свежие партии сверху. Тап по карточке — итоги партии
 * (ResultScreen). Пустое состояние: «Пока нет сыгранных
 * партий» + кнопка «НОВАЯ ИГРА». Stateless: принимает
 * UI-state целиком и callbacks.
 *
 * @param uiState Состояние списка партий.
 * @param onBack Колбэк «назад».
 * @param onGameClick Колбэк выбора партии (gameId).
 * @param onNewGame Колбэк «НОВАЯ ИГРА».
 * @param onErrorShown Колбэк «ошибка показана» (сброс errorMessage).
 * @param modifier Модификатор.
 */
@Composable
fun GameHistoryScreen(
    uiState: GameHistoryUiState,
    onBack: () -> Unit,
    onGameClick: (String) -> Unit,
    onNewGame: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CenteredContent {
            Column(modifier = Modifier.fillMaxSize()) {
                KolhozTopBar(title = "ИСТОРИЯ ИГР", onBackClick = onBack)

                if (uiState.isEmpty) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(KolhozSpacing.L),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Пока нет сыгранных партий.",
                            style = KolhozTypography.H2,
                            color = KolhozColors.TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(KolhozSpacing.M))
                        Text(
                            text = "Создайте первую игру, чтобы она появилась здесь.",
                            style = KolhozTypography.Body,
                            color = KolhozColors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(KolhozSpacing.XXL))
                        KolhozButton(
                            text = "НОВАЯ ИГРА",
                            onClick = onNewGame,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(KolhozSpacing.L),
                        verticalArrangement = Arrangement.spacedBy(KolhozSpacing.M),
                    ) {
                        items(uiState.games, key = { it.gameId }) { game ->
                            GameHistoryCard(
                                date = game.date,
                                players = game.players.map {
                                    PlayerScore(name = it.name, score = it.score)
                                },
                                shotCount = game.shotsCount,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onGameClick(game.gameId) },
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun previewState(): GameHistoryUiState = GameHistoryUiState(
    games = listOf(
        GameHistoryItemUi(
            gameId = "g1",
            date = "08.10.2026",
            playersCount = 4,
            shotsCount = 42,
            players = listOf(
                PlayerResultUi(1, "Саша", 12),
                PlayerResultUi(2, "Коля", 4),
                PlayerResultUi(3, "Петя", -5),
                PlayerResultUi(4, "Дима", -11),
            ),
        ),
        GameHistoryItemUi(
            gameId = "g2",
            date = "07.10.2026",
            playersCount = 3,
            shotsCount = 28,
            players = listOf(
                PlayerResultUi(1, "Коля", 9),
                PlayerResultUi(2, "Саша", 1),
                PlayerResultUi(3, "Петя", -10),
            ),
        ),
    ),
    isEmpty = false,
)

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun GameHistoryScreenPreview() {
    KolkhozTheme {
        GameHistoryScreen(
            uiState = previewState(),
            onBack = {},
            onGameClick = {},
            onNewGame = {},
            onErrorShown = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun GameHistoryScreenEmptyPreview() {
    KolkhozTheme {
        GameHistoryScreen(
            uiState = GameHistoryUiState(games = emptyList(), isEmpty = true),
            onBack = {},
            onGameClick = {},
            onNewGame = {},
            onErrorShown = {},
        )
    }
}
