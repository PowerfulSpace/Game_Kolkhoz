package ru.kolkhoz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
            val viewModel: NewGameViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            NewGameScreen(
                uiState = uiState,
                onPlayerCountChange = viewModel::onPlayerCountChange,
                onPlayerNameChange = viewModel::onPlayerNameChange,
                onDeletePlayer = viewModel::onDeletePlayer,
                onBack = { navController.popBackStack() },
                onStartGame = {
                    viewModel.onStartGame { _: GameId ->
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.NEW_GAME) { inclusive = true }
                        }
                    }
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
