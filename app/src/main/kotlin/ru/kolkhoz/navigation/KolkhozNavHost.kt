package ru.kolkhoz.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.ui.screens.GameScreen
import ru.kolkhoz.ui.screens.HistoryScreen
import ru.kolkhoz.ui.screens.HomeScreen
import ru.kolkhoz.ui.screens.NewGameScreen
import ru.kolkhoz.ui.screens.ResultScreen
import ru.kolkhoz.ui.viewmodel.GameViewModel
import ru.kolkhoz.ui.viewmodel.HomeViewModel
import ru.kolkhoz.ui.viewmodel.NewGameViewModel
import ru.kolkhoz.ui.viewmodel.ResultViewModel

/**
 * Маршруты графа навигации (UI_SPEC.md, раздел 7.1).
 *
 * [RESULT] — единственный маршрут с аргументом: `gameId`
 * завершённой партии.
 */
object Routes {

    /** Главный экран — корень back-stack. */
    const val HOME = "home"

    /** Создание новой партии. */
    const val NEW_GAME = "new_game"

    /** Игровой экран (берёт активную партию). */
    const val GAME = "game"

    /** История ударов текущей партии. */
    const val HISTORY = "history"

    /** Итоги партии; аргумент `gameId`. */
    const val RESULT = "result/{gameId}"

    /** Формирует маршрут итогов для конкретной партии. */
    fun result(gameId: String): String = "result/$gameId"
}

/**
 * Граф навигации приложения: 5 экранов, один `NavHost`
 * (UI_SPEC.md, раздел 1.1).
 *
 * - HOME → NEW_GAME / GAME / HISTORY.
 * - NEW_GAME → GAME (после «НАЧАТЬ ИГРУ»; NEW_GAME убирается
 *   из back-stack, вернуться нельзя — UI_SPEC 7.3).
 * - GAME → HISTORY / RESULT (после завершения; GAME убирается
 *   из back-stack — UI_SPEC 7.3).
 * - HISTORY → назад (popBackStack).
 * - RESULT → NEW_GAME / HOME (back-stack очищается — UI_SPEC 7.3).
 *
 * Каждый экран получает свой экземпляр ViewModel через
 * `hiltViewModel()` (привязан к NavBackStackEntry).
 *
 * @param navController Контроллер навигации (по умолчанию создаётся).
 */
@Composable
fun KolkhozNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            LockPortrait()
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                uiState = uiState,
                onNewGame = { navController.navigate(Routes.NEW_GAME) },
                onContinueGame = { navController.navigate(Routes.GAME) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onErrorShown = viewModel::clearError,
            )
        }

        composable(Routes.NEW_GAME) {
            LockPortrait()
            val viewModel: NewGameViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var showActiveGameDialog by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            // Начало игры: проверка активной партии (UI_SPEC.md 3.4, 8.2).
            val startGame = {
                viewModel.onStartGame { _: GameId ->
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.NEW_GAME) { inclusive = true }
                    }
                }
            }

            NewGameScreen(
                uiState = uiState,
                onPlayerCountChange = viewModel::onPlayerCountChange,
                onPlayerNameChange = viewModel::onPlayerNameChange,
                onDeletePlayer = viewModel::onDeletePlayer,
                onBack = { navController.popBackStack() },
                onStartGame = {
                    scope.launch {
                        if (viewModel.hasActiveGame()) {
                            showActiveGameDialog = true
                        } else {
                            startGame()
                        }
                    }
                },
                showActiveGameDialog = showActiveGameDialog,
                onConfirmFinishActive = {
                    scope.launch {
                        viewModel.finishActiveGame()
                        startGame()
                    }
                },
                onCancelFinishActive = {
                    showActiveGameDialog = false
                },
                onErrorShown = viewModel::clearError,
            )
        }

        composable(Routes.GAME) {
            val viewModel: GameViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
            GameScreen(
                uiState = uiState,
                isLoading = isLoading,
                onPocket = viewModel::onPocket,
                onMiss = viewModel::onMiss,
                onUndo = viewModel::onUndo,
                onRedo = viewModel::onRedo,
                onHistory = { navController.navigate(Routes.HISTORY) },
                onFinish = {
                    viewModel.onFinishGame { gameId ->
                        navController.navigate(Routes.result(gameId.value)) {
                            popUpTo(Routes.GAME) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
                onErrorShown = viewModel::clearError,
            )
        }

        composable(Routes.HISTORY) {
            // Отдельный экземпляр GameViewModel: подписывается на
            // активную партию и отдаёт historyUiState (ЧАСТЬ 3.1).
            val viewModel: GameViewModel = hiltViewModel()
            val uiState by viewModel.historyUiState.collectAsStateWithLifecycle()
            HistoryScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
            ),
        ) {
            LockPortrait()
            val viewModel: ResultViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ResultScreen(
                uiState = uiState,
                onNewGame = {
                    navController.navigate(Routes.NEW_GAME) {
                        popUpTo(Routes.HOME)
                    }
                },
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onErrorShown = viewModel::clearError,
            )
        }
    }
}

/**
 * Блокирует ориентацию экрана в portrait на время жизни
 * composable (UI_SPEC.md 1.2: Home, NewGame, Result — только
 * portrait; Game и History работают в обеих ориентациях).
 *
 * При выходе из composable исходная ориентация восстанавливается.
 */
@Composable
private fun LockPortrait() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val original = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation =
                original ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

/** Находит Activity из контекста Compose (обход ContextWrapper). */
private fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
