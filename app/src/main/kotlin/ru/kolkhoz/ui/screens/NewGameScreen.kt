package ru.kolkhoz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.ui.components.KolhozButton
import ru.kolkhoz.ui.components.KolhozTopBar
import ru.kolkhoz.ui.components.PlayerCountSelector
import ru.kolkhoz.ui.components.PlayerInput
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Экран создания игры (UI_SPEC.md, раздел 3).
 *
 * Счётчик игроков 3–8, ввод имён, кнопка «НАЧАТЬ ИГРУ».
 * Inline-валидация в этом MVP-этапе не показывается
 * (`errorText = null`), валидация имён — в Phase 4.
 * Stateless: данные + callbacks, состояние не хранит.
 *
 * @param playerCount Текущее количество игроков.
 * @param playerNames Имена игроков (по элементу на игрока).
 * @param onPlayerCountChange Колбэк смены количества.
 * @param onPlayerNameChange Колбэк смены имени (индекс, текст).
 * @param onDeletePlayer Колбэк удаления игрока (индекс).
 * @param onBack Колбэк «назад».
 * @param onStartGame Колбэк «НАЧАТЬ ИГРУ».
 * @param canStartGame Можно ли начать игру (все имена валидны).
 * @param modifier Модификатор.
 */
@Composable
fun NewGameScreen(
    playerCount: Int,
    playerNames: List<String>,
    onPlayerCountChange: (Int) -> Unit,
    onPlayerNameChange: (Int, String) -> Unit,
    onDeletePlayer: (Int) -> Unit,
    onBack: () -> Unit,
    onStartGame: () -> Unit,
    canStartGame: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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
                value = playerCount,
                onDecrease = { onPlayerCountChange(playerCount - 1) },
                onIncrease = { onPlayerCountChange(playerCount + 1) },
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
            for (i in 0 until playerCount) {
                PlayerInput(
                    value = playerNames[i],
                    onValueChange = { onPlayerNameChange(i, it) },
                    playerNumber = i + 1,
                    onDelete = { onDeletePlayer(i) },
                    maxLength = 32,
                    errorText = null,
                )
            }
        }

        Spacer(modifier = Modifier.height(KolhozSpacing.XXL))

        KolhozButton(
            text = "НАЧАТЬ ИГРУ",
            onClick = onStartGame,
            enabled = canStartGame,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun NewGameScreenPreview() {
    KolkhozTheme {
        NewGameScreen(
            playerCount = 4,
            playerNames = listOf("Саша", "Петя", "Коля", "Дима"),
            onPlayerCountChange = {},
            onPlayerNameChange = { _, _ -> },
            onDeletePlayer = {},
            onBack = {},
            onStartGame = {},
            canStartGame = true,
        )
    }
}
