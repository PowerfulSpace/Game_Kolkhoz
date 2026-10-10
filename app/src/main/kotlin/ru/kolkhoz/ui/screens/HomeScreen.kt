package ru.kolkhoz.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.R
import ru.kolkhoz.ui.components.CenteredContent
import ru.kolkhoz.ui.components.KolhozButton
import ru.kolkhoz.ui.components.KolhozSecondaryButton
import ru.kolkhoz.ui.components.KolkhozIcons
import ru.kolkhoz.ui.model.HomeUiState
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Главный экран (UI_SPEC.md, раздел 2).
 *
 * Точка входа: новая игра, продолжить, история.
 * Кнопка «ПРОДОЛЖИТЬ ИГРУ» показывается только при
 * `uiState.hasActiveGame` (скрыта, не disabled).
 * Stateless: принимает UI-state целиком и callbacks.
 *
 * @param uiState Состояние главного экрана.
 * @param onNewGame Колбэк «НОВАЯ ИГРА».
 * @param onContinueGame Колбэк «ПРОДОЛЖИТЬ ИГРУ».
 * @param onHistory Колбэк «ИСТОРИЯ ИГР».
 * @param onErrorShown Колбэк «ошибка показана» (сброс errorMessage).
 * @param modifier Модификатор.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNewGame: () -> Unit,
    onContinueGame: () -> Unit,
    onHistory: () -> Unit,
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

    // Во время загрузки (isLoading) показываем экран как есть:
    // до первого emission активной партии нет — кнопка скрыта.
    Box(modifier = modifier.fillMaxSize()) {
        // Фон: фото стола в нижних 40% экрана, приглушённое.
        // Снаружи CenteredContent — на всю ширину (в т.ч. landscape).
        Image(
            painter = painterResource(R.drawable.home_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            alpha = 0.25f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
        )

        CenteredContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = KolhozSpacing.XXL),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Логотип + подписи — один плотный блок по центру.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_kolkhoz),
                        contentDescription = "КОЛХОЗ",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth(0.8f),
                    )
                    Spacer(Modifier.height(KolhozSpacing.L))
                    Text(
                        text = "БИЛЬЯРДНЫЙ СЧЁТЧИК",
                        style = KolhozTypography.Body,
                        color = KolhozColors.TextSecondary,
                    )
                    Spacer(Modifier.height(KolhozSpacing.S))
                    Text(
                        text = "Считаем, чтобы не сраться",
                        style = KolhozTypography.Caption,
                        color = KolhozColors.TextSecondary,
                    )
                }

                // Отступ от слогана до первой кнопки — 40dp.
                Spacer(Modifier.height(KolhozSpacing.XXXXL))

                // Между кнопками — 16dp.
                Column(
                    verticalArrangement = Arrangement.spacedBy(KolhozSpacing.L),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    KolhozButton(
                        text = "НОВАЯ ИГРА",
                        onClick = onNewGame,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = KolkhozIcons.NewGame,
                    )
                    if (uiState.hasActiveGame) {
                        KolhozSecondaryButton(
                            text = "ПРОДОЛЖИТЬ ИГРУ",
                            onClick = onContinueGame,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = KolkhozIcons.Continue,
                        )
                    }
                    KolhozSecondaryButton(
                        text = "ИСТОРИЯ ИГР",
                        onClick = onHistory,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = KolkhozIcons.History,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun HomeScreenPreview() {
    KolkhozTheme {
        HomeScreen(
            uiState = HomeUiState(hasActiveGame = true),
            onNewGame = {},
            onContinueGame = {},
            onHistory = {},
            onErrorShown = {},
        )
    }
}
