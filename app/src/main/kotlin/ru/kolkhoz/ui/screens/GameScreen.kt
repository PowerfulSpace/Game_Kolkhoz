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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import android.content.res.Configuration
import ru.kolkhoz.ui.components.CurrentPlayerCard
import ru.kolkhoz.ui.components.KolhozBottomBar
import ru.kolkhoz.ui.components.KolhozButton
import ru.kolkhoz.ui.components.KolhozSecondaryButton
import ru.kolkhoz.ui.components.KolhozTopBar
import ru.kolkhoz.ui.components.PlayerRow
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Временная модель игрока для отображения (Phase 3b).
 *
 * Только для отображения. В Phase 4 будет заменена
 * на реальную UI-модель из ViewModel.
 *
 * @param id Идентификатор игрока.
 * @param name Имя игрока.
 * @param score Счёт игрока.
 * @param isCurrent Ходит ли сейчас этот игрок.
 */
data class PlayerUi(
    val id: String,
    val name: String,
    val score: Int,
    val isCurrent: Boolean,
)

/**
 * Игровой экран (UI_SPEC.md, разделы 4 и 4.6).
 *
 * Портрет: карточка текущего игрока, список, кнопки «ЗАБИЛ»/«ПРОМАХ».
 * Ландшафт (4.6): три колонки — игроки, текущий, действия.
 * Меню «⋮» — placeholder (DropdownMenu: История / Завершить игру).
 * Stateless: данные + callbacks, состояние не хранит
 * (кроме композиционного состояния раскрытия меню).
 *
 * @param players Игроки партии.
 * @param currentPlayerId id текущего игрока.
 * @param currentStreak Текущая серия ударов подряд.
 * @param canUndo Можно ли отменить последний удар.
 * @param onPocket Колбэк «ЗАБИЛ».
 * @param onMiss Колбэк «ПРОМАХ».
 * @param onUndo Колбэк «отмена удара».
 * @param onHistory Колбэк «история».
 * @param onFinish Колбэк «завершить игру».
 * @param modifier Модификатор.
 */
@Composable
fun GameScreen(
    players: List<PlayerUi>,
    currentPlayerId: String,
    currentStreak: Int,
    canUndo: Boolean,
    onPocket: () -> Unit,
    onMiss: () -> Unit,
    onUndo: () -> Unit,
    onHistory: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val current = players.firstOrNull { it.id == currentPlayerId }

    Column(modifier = modifier.fillMaxSize()) {
        Box {
            KolhozTopBar(
                title = "КОЛХОЗ",
                onMenuClick = { menuExpanded = true },
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                DropdownMenuItem(
                    text = { Text("История") },
                    onClick = {
                        menuExpanded = false
                        onHistory()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Завершить игру") },
                    onClick = {
                        menuExpanded = false
                        onFinish()
                    },
                )
            }
        }

        if (isLandscape) {
            LandscapeContent(
                players = players,
                current = current,
                currentStreak = currentStreak,
                onPocket = onPocket,
                onMiss = onMiss,
                modifier = Modifier.weight(1f),
            )
        } else {
            PortraitContent(
                players = players,
                current = current,
                currentStreak = currentStreak,
                onPocket = onPocket,
                onMiss = onMiss,
                modifier = Modifier.weight(1f),
            )
        }

        KolhozBottomBar(
            onUndoClick = onUndo,
            onHistoryClick = onHistory,
            undoEnabled = canUndo,
        )
    }
}

@Composable
private fun PortraitContent(
    players: List<PlayerUi>,
    current: PlayerUi?,
    currentStreak: Int,
    onPocket: () -> Unit,
    onMiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(KolhozSpacing.L),
    ) {
        if (current != null) {
            CurrentPlayerCard(
                playerName = current.name,
                score = current.score,
                series = currentStreak,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(KolhozSpacing.M))
        HorizontalDivider(
            color = KolhozColors.Border.copy(alpha = 0.3f),
            thickness = 1.dp,
        )
        Spacer(modifier = Modifier.height(KolhozSpacing.M))
        Column(verticalArrangement = Arrangement.spacedBy(KolhozSpacing.XS)) {
            players.forEach { player ->
                PlayerRow(
                    name = player.name,
                    score = player.score,
                    isCurrent = player.isCurrent,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        KolhozButton(
            text = "ЗАБИЛ",
            onClick = onPocket,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(KolhozSpacing.M))
        KolhozSecondaryButton(
            text = "ПРОМАХ",
            onClick = onMiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LandscapeContent(
    players: List<PlayerUi>,
    current: PlayerUi?,
    currentStreak: Int,
    onPocket: () -> Unit,
    onMiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(KolhozSpacing.L),
    ) {
        // Левая колонка — список игроков.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ИГРОКИ",
                style = KolhozTypography.H2,
                color = KolhozColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(KolhozSpacing.M))
            players.forEach { player ->
                PlayerRow(
                    name = player.name,
                    score = player.score,
                    isCurrent = player.isCurrent,
                )
            }
        }
        // Центральная колонка — текущий игрок.
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "СЕЙЧАС БЬЁТ",
                style = KolhozTypography.H2,
                color = KolhozColors.TextPrimary,
            )
            if (current != null) {
                CurrentPlayerCard(
                    playerName = current.name,
                    score = current.score,
                    series = currentStreak,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        // Правая колонка — действия.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ДЕЙСТВИЕ",
                style = KolhozTypography.H2,
                color = KolhozColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(KolhozSpacing.M))
            KolhozButton(
                text = "ЗАБИЛ",
                onClick = onPocket,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(KolhozSpacing.M))
            KolhozSecondaryButton(
                text = "ПРОМАХ",
                onClick = onMiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun GameScreenPortraitPreview() {
    KolkhozTheme {
        GameScreen(
            players = mockPlayers(),
            currentPlayerId = "p1",
            currentStreak = 4,
            canUndo = true,
            onPocket = {},
            onMiss = {},
            onUndo = {},
            onHistory = {},
            onFinish = {},
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF08110F,
    widthDp = 800,
    heightDp = 400,
)
@Composable
private fun GameScreenLandscapePreview() {
    KolkhozTheme {
        GameScreen(
            players = mockPlayers(),
            currentPlayerId = "p1",
            currentStreak = 4,
            canUndo = true,
            onPocket = {},
            onMiss = {},
            onUndo = {},
            onHistory = {},
            onFinish = {},
        )
    }
}

private fun mockPlayers(): List<PlayerUi> = listOf(
    PlayerUi("p1", "Саша", 7, true),
    PlayerUi("p2", "Петя", 2, false),
    PlayerUi("p3", "Коля", -4, false),
    PlayerUi("p4", "Дима", -5, false),
)
