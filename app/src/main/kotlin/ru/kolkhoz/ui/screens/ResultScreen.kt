package ru.kolkhoz.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.R
import ru.kolkhoz.ui.components.KolkhozIcons
import ru.kolkhoz.ui.components.KolhozButton
import ru.kolkhoz.ui.components.KolhozSecondaryButton
import ru.kolkhoz.ui.components.ScoreText
import ru.kolkhoz.ui.components.rowScoreStyle
import ru.kolkhoz.ui.model.PlayerResultUi
import ru.kolkhoz.ui.model.ResultUiState
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Экран результатов партии (UI_SPEC.md, раздел 6).
 *
 * Кубок, итоговая таблица с выделением победителя
 * (фон Primary 20%, рамка Primary, корона) и кнопки.
 * Stateless: принимает UI-state целиком и callbacks.
 *
 * Состояния (UI_SPEC.md 1.3): Loading — спиннер; пустая
 * «Партия не найдена» с кнопкой «НА ГЛАВНУЮ»; иначе Content.
 *
 * @param uiState Состояние экрана итогов (игроки по местам).
 * @param onNewGame Колбэк «НОВАЯ ИГРА».
 * @param onHome Колбэк «НА ГЛАВНУЮ».
 * @param onErrorShown Колбэк «ошибка показана» (сброс errorMessage).
 * @param modifier Модификатор.
 */
@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onNewGame: () -> Unit,
    onHome: () -> Unit,
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

    // Загрузка партии (UI_SPEC.md 1.3 — состояние Loading).
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = KolhozColors.Primary)
        }
        return
    }

    // Партия не найдена — без пустого «ИГРА ОКОНЧЕНА».
    if (uiState.players.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(KolhozSpacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Партия не найдена",
                style = KolhozTypography.H2,
                color = KolhozColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(KolhozSpacing.XXL))
            KolhozSecondaryButton(
                text = "НА ГЛАВНУЮ",
                onClick = onHome,
            )
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.trophy),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(200.dp),
            )

            Text(
                text = "ИГРА ОКОНЧЕНА",
                style = KolhozTypography.H1,
                color = KolhozColors.TextPrimary,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(KolhozSpacing.S),
            ) {
                uiState.players.forEach { player ->
                    ResultRow(player = player)
                }
            }

            KolhozButton(
                text = "НОВАЯ ИГРА",
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth(),
            )
            KolhozSecondaryButton(
                text = "НА ГЛАВНУЮ",
                onClick = onHome,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ResultRow(player: PlayerResultUi) {
    val isWinner = player.place == 1
    val shape = RoundedCornerShape(KolhozRadius.M)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (isWinner) {
                    KolhozColors.Primary.copy(alpha = 0.2f)
                } else {
                    KolhozColors.Surface
                },
            )
            .border(
                width = if (isWinner) 1.dp else 0.dp,
                color = KolhozColors.Primary,
                shape = shape,
            )
            .padding(KolhozSpacing.L),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KolhozSpacing.M),
    ) {
        if (isWinner) {
            Icon(
                imageVector = KolkhozIcons.Crown,
                contentDescription = "Победитель",
                tint = KolhozColors.Warning,
                modifier = Modifier.width(24.dp),
            )
        }
        Text(
            text = player.place.toString(),
            style = KolhozTypography.H2,
            color = KolhozColors.TextSecondary,
        )
        Text(
            text = player.name,
            style = KolhozTypography.Body,
            color = KolhozColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        ScoreText(score = player.score, style = rowScoreStyle)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun ResultScreenPreview() {
    KolkhozTheme {
        ResultScreen(
            uiState = ResultUiState(
                players = listOf(
                    PlayerResultUi(1, "Саша", 12),
                    PlayerResultUi(2, "Коля", 4),
                    PlayerResultUi(3, "Петя", -5),
                    PlayerResultUi(4, "Дима", -11),
                ),
            ),
            onNewGame = {},
            onHome = {},
            onErrorShown = {},
        )
    }
}
