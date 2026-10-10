package ru.kolkhoz.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.kolkhoz.R
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.ui.components.CurrentPlayerCard
import ru.kolkhoz.ui.components.KolhozBottomBar
import ru.kolkhoz.ui.components.KolhozButton
import ru.kolkhoz.ui.components.KolhozDialog
import ru.kolkhoz.ui.components.KolhozSecondaryButton
import ru.kolkhoz.ui.components.KolhozTopBar
import ru.kolkhoz.ui.components.PlayerRow
import ru.kolkhoz.ui.model.GameUiState
import ru.kolkhoz.ui.model.PlayerUi
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

private const val UNDO_MESSAGE = "Удар отменён"
private const val UNDO_ACTION = "ВЕРНУТЬ"
private const val GAME_NOT_FOUND_MESSAGE = "Партия не найдена"

/**
 * Игровой экран (UI_SPEC.md, разделы 4 и 4.6).
 *
 * Портрет: карточка текущего игрока, список, кнопки «ЗАБИЛ»/«ПРОМАХ».
 * Ландшафт (4.6): три колонки — игроки, текущий, действия.
 * Меню «⋮» — DropdownMenu (История / Завершить игру).
 *
 * Состояния:
 * - `isLoading` — индикатор загрузки (борьба с мельканием);
 * - `uiState == null` — «Партия не найдена» + кнопка «НА ГЛАВНУЮ»;
 * - иначе — игровой контент.
 *
 * Snackbar: после undo — «Удар отменён» с action «ВЕРНУТЬ»
 * (redo, UI_SPEC 9.1); ошибки из `uiState.errorMessage`.
 *
 * Stateless: принимает UI-state целиком и callbacks.
 *
 * @param uiState Состояние партии; `null` — активной партии нет.
 * @param isLoading Идёт ли первая загрузка партии.
 * @param onPocket Колбэк «ЗАБИЛ».
 * @param onMiss Колбэк «ПРОМАХ».
 * @param onUndo Колбэк «отмена удара».
 * @param onRedo Колбэк «повтор отменённого удара».
 * @param onHistory Колбэк «история».
 * @param onFinish Колбэк «завершить игру».
 * @param onBack Колбэк «назад» (в null-состоянии — на главную).
 * @param onErrorShown Колбэк «ошибка показана» (сброс errorMessage).
 * @param modifier Модификатор.
 */
@Composable
fun GameScreen(
    uiState: GameUiState?,
    isLoading: Boolean,
    onPocket: () -> Unit,
    onMiss: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onHistory: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Ошибки (сохранение и т.п.) — в snackbar, затем сбрасываем.
    LaunchedEffect(uiState?.errorMessage) {
        uiState?.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )

            uiState == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(KolhozSpacing.L),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = GAME_NOT_FOUND_MESSAGE,
                    style = KolhozTypography.H2,
                    color = KolhozColors.TextSecondary,
                )
                Spacer(modifier = Modifier.height(KolhozSpacing.L))
                KolhozSecondaryButton(
                    text = "НА ГЛАВНУЮ",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            else -> GameContent(
                uiState = uiState,
                onPocket = onPocket,
                onMiss = onMiss,
                onUndo = {
                    onUndo()
                    // UI_SPEC 9.1: undo → snackbar c «ВЕРНУТЬ» (redo).
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = UNDO_MESSAGE,
                            actionLabel = UNDO_ACTION,
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onRedo()
                        }
                    }
                },
                onHistory = onHistory,
                onFinish = onFinish,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun GameContent(
    uiState: GameUiState,
    onPocket: () -> Unit,
    onMiss: () -> Unit,
    onUndo: () -> Unit,
    onHistory: () -> Unit,
    onFinish: () -> Unit,
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val current = uiState.players.firstOrNull { it.id == uiState.currentPlayerId }
    var menuExpanded by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    // Пока идёт сохранение — кнопки удара заблокированы (дабл-тап).
    val buttonsEnabled = !uiState.isSaving

    Column(modifier = Modifier.fillMaxSize()) {
        Box {
            KolhozTopBar(
                logoRes = R.drawable.logo_topbar,
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
                        // UI_SPEC.md 4.4, 8.1: сначала диалог подтверждения.
                        showFinishDialog = true
                    },
                )
            }
        }

        if (showFinishDialog) {
            KolhozDialog(
                title = "ЗАВЕРШИТЬ ИГРУ?",
                text = "Текущая партия будет сохранена в истории.",
                confirmText = "ЗАВЕРШИТЬ",
                onConfirm = {
                    showFinishDialog = false
                    onFinish()
                },
                cancelText = "ОТМЕНА",
                onDismiss = { showFinishDialog = false },
            )
        }

        if (isLandscape) {
            LandscapeContent(
                players = uiState.players,
                current = current,
                currentStreak = uiState.currentStreak,
                buttonsEnabled = buttonsEnabled,
                onPocket = onPocket,
                onMiss = onMiss,
                modifier = Modifier.weight(1f),
            )
        } else {
            PortraitContent(
                players = uiState.players,
                current = current,
                currentStreak = uiState.currentStreak,
                buttonsEnabled = buttonsEnabled,
                onPocket = onPocket,
                onMiss = onMiss,
                modifier = Modifier.weight(1f),
            )
        }

        KolhozBottomBar(
            onUndoClick = onUndo,
            onHistoryClick = onHistory,
            undoEnabled = uiState.canUndo,
        )
    }
}

@Composable
private fun PortraitContent(
    players: List<PlayerUi>,
    current: PlayerUi?,
    currentStreak: Int,
    buttonsEnabled: Boolean,
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
            val leaderId = players.maxByOrNull { it.score }?.id
            players.forEachIndexed { index, player ->
                PlayerRow(
                    name = player.name,
                    score = player.score,
                    isCurrent = player.isCurrent,
                    number = index + 1,
                    isLeader = player.id == leaderId,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        KolhozButton(
            text = "ЗАБИЛ",
            enabled = buttonsEnabled,
            onClick = onPocket,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(KolhozSpacing.M))
        KolhozSecondaryButton(
            text = "ПРОМАХ",
            enabled = buttonsEnabled,
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
    buttonsEnabled: Boolean,
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
            val leaderId = players.maxByOrNull { it.score }?.id
            players.forEachIndexed { index, player ->
                PlayerRow(
                    name = player.name,
                    score = player.score,
                    isCurrent = player.isCurrent,
                    number = index + 1,
                    isLeader = player.id == leaderId,
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
                enabled = buttonsEnabled,
                onClick = onPocket,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(KolhozSpacing.M))
            KolhozSecondaryButton(
                text = "ПРОМАХ",
                enabled = buttonsEnabled,
                onClick = onMiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun previewState(): GameUiState = GameUiState(
    players = listOf(
        PlayerUi(PlayerId("p1"), "Саша", 7, true),
        PlayerUi(PlayerId("p2"), "Петя", 2, false),
        PlayerUi(PlayerId("p3"), "Коля", -4, false),
        PlayerUi(PlayerId("p4"), "Дима", -5, false),
    ),
    currentPlayerId = PlayerId("p1"),
    currentStreak = 4,
    canUndo = true,
    status = GameStatus.ACTIVE,
    lastUndoneEvent = null,
)

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun GameScreenPortraitPreview() {
    KolkhozTheme {
        GameScreen(
            uiState = previewState(),
            isLoading = false,
            onPocket = {},
            onMiss = {},
            onUndo = {},
            onRedo = {},
            onHistory = {},
            onFinish = {},
            onBack = {},
            onErrorShown = {},
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
            uiState = previewState(),
            isLoading = false,
            onPocket = {},
            onMiss = {},
            onUndo = {},
            onRedo = {},
            onHistory = {},
            onFinish = {},
            onBack = {},
            onErrorShown = {},
        )
    }
}
