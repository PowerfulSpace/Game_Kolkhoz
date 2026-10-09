package ru.kolkhoz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.ui.components.KolhozButton
import ru.kolkhoz.ui.components.KolhozDialog
import ru.kolkhoz.ui.components.KolhozTopBar
import ru.kolkhoz.ui.components.PlayerCountSelector
import ru.kolkhoz.ui.components.PlayerInput
import ru.kolkhoz.ui.model.NewGameUiState
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Экран создания игры (UI_SPEC.md, раздел 3).
 *
 * Счётчик игроков 3–8, ввод имён, кнопка «НАЧАТЬ ИГРУ»
 * (disabled, пока имена невалидны — `uiState.canStartGame`).
 * Stateless: принимает UI-state целиком и callbacks.
 *
 * Диалог «У ВАС ЕСТЬ АКТИВНАЯ ПАРТИЯ» (UI_SPEC.md 3.4, 8.2)
 * управляется состоянием [showActiveGameDialog] извне —
 * владелец (NavHost) решает, показывать ли его.
 *
 * @param uiState Состояние экрана создания игры.
 * @param onPlayerCountChange Колбэк смены количества игроков.
 * @param onPlayerNameChange Колбэк смены имени (индекс, текст).
 * @param onDeletePlayer Колбэк удаления игрока (индекс).
 * @param onBack Колбэк «назад».
 * @param onStartGame Колбэк «НАЧАТЬ ИГРУ» (проверка активной партии — у владельца).
 * @param showActiveGameDialog Показывать ли диалог активной партии.
 * @param onConfirmFinishActive Подтверждение: завершить активную и создать новую.
 * @param onCancelFinishActive Отмена диалога активной партии.
 * @param onErrorShown Колбэк «ошибка показана» (сброс errorMessage).
 * @param modifier Модификатор.
 */
@Composable
fun NewGameScreen(
    uiState: NewGameUiState,
    onPlayerCountChange: (Int) -> Unit,
    onPlayerNameChange: (Int, String) -> Unit,
    onDeletePlayer: (Int) -> Unit,
    onBack: () -> Unit,
    onStartGame: () -> Unit,
    showActiveGameDialog: Boolean,
    onConfirmFinishActive: () -> Unit,
    onCancelFinishActive: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.L),
        ) {
            KolhozTopBar(title = "НОВАЯ ИГРА", onBackClick = onBack)

            Text(
                text = "СКОЛЬКО ИГРОКОВ?",
                style = KolhozTypography.H2,
                color = KolhozColors.TextPrimary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                PlayerCountSelector(
                    value = uiState.playerCount,
                    onDecrease = { onPlayerCountChange(uiState.playerCount - 1) },
                    onIncrease = { onPlayerCountChange(uiState.playerCount + 1) },
                    min = 3,
                    max = 8,
                )
            }

            Text(
                text = "ОТ 3 ДО 8 ИГРОКОВ",
                style = KolhozTypography.Caption,
                color = KolhozColors.TextSecondary,
            )

            Text(
                text = "ИГРОКИ",
                style = KolhozTypography.H2,
                color = KolhozColors.TextPrimary,
            )

            Column(verticalArrangement = Arrangement.spacedBy(KolhozSpacing.M)) {
                for (i in 0 until uiState.playerCount) {
                    PlayerInput(
                        value = uiState.playerNames[i],
                        onValueChange = { onPlayerNameChange(i, it) },
                        playerNumber = i + 1,
                        onDelete = { onDeletePlayer(i) },
                        maxLength = 32,
                        // Inline-ошибки — только после «НАЧАТЬ ИГРУ».
                        errorText = if (uiState.wasSubmitted) {
                            uiState.nameErrors.getOrNull(i)
                        } else {
                            null
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(KolhozSpacing.XXL))

            KolhozButton(
                text = "НАЧАТЬ ИГРУ",
                onClick = onStartGame,
                // Кнопка активна всегда: тап = валидация имён
                // (wasSubmitted), блокируется только на время сохранения.
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (showActiveGameDialog) {
            KolhozDialog(
                title = "У ВАС ЕСТЬ АКТИВНАЯ ПАРТИЯ",
                text = "Завершить её и создать новую?",
                confirmText = "ЗАВЕРШИТЬ",
                onConfirm = onConfirmFinishActive,
                cancelText = "ОТМЕНА",
                onDismiss = onCancelFinishActive,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun NewGameScreenPreview() {
    KolkhozTheme {
        NewGameScreen(
            uiState = NewGameUiState(
                playerCount = 4,
                playerNames = listOf("Саша", "Петя", "", "Дима"),
                canStartGame = false,
                nameErrors = listOf(null, null, "Введите имя игрока", null),
            ),
            onPlayerCountChange = {},
            onPlayerNameChange = { _, _ -> },
            onDeletePlayer = {},
            onBack = {},
            onStartGame = {},
            showActiveGameDialog = false,
            onConfirmFinishActive = {},
            onCancelFinishActive = {},
            onErrorShown = {},
        )
    }
}
