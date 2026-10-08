package ru.kolkhoz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import ru.kolkhoz.ui.screens.GameScreen
import ru.kolkhoz.ui.screens.HistoryScreen
import ru.kolkhoz.ui.screens.HomeScreen
import ru.kolkhoz.ui.screens.NewGameScreen
import ru.kolkhoz.ui.screens.PlayerResultUi
import ru.kolkhoz.ui.screens.PlayerUi
import ru.kolkhoz.ui.screens.ResultScreen
import ru.kolkhoz.ui.screens.ShotUi
import ru.kolkhoz.ui.theme.KolkhozTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KolkhozTheme {
                MockNavigation()
            }
        }
    }
}

// ВРЕМЕННО: mock-навигация, заменим в Phase 4 на ViewModel + NavHost.
/**
 * Временная навигация для визуальной проверки Phase 3b.
 *
 * В Phase 4 будет заменена на ViewModel + NavHost.
 * Хранит имя экрана в [remember], передаёт mock-данные,
 * переключает экраны по callbacks. Stateless-экраны
 * бизнес-состояния не хранят.
 */
@Composable
private fun MockNavigation() {
    // ВРЕМЕННО: mock-навигация для визуальной проверки Phase 3b.
    // В Phase 4 будет заменена на ViewModel + NavHost.
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeScreen(
            onNewGame = { currentScreen = "new_game" },
            onContinueGame = { currentScreen = "game" },
            onHistory = { currentScreen = "history" },
            hasActiveGame = true,
        )

        "new_game" -> NewGameScreen(
            playerCount = 4,
            playerNames = listOf("Саша", "Петя", "Коля", "Дима"),
            onPlayerCountChange = { /* mock */ },
            onPlayerNameChange = { _, _ -> /* mock */ },
            onDeletePlayer = { /* mock */ },
            onBack = { currentScreen = "home" },
            onStartGame = { currentScreen = "game" },
            canStartGame = true,
        )

        "game" -> GameScreen(
            players = listOf(
                PlayerUi("p1", "Саша", 7, true),
                PlayerUi("p2", "Петя", 2, false),
                PlayerUi("p3", "Коля", -4, false),
                PlayerUi("p4", "Дима", -5, false),
            ),
            currentPlayerId = "p1",
            currentStreak = 4,
            canUndo = true,
            onPocket = { /* mock */ },
            onMiss = { /* mock */ },
            onUndo = { /* mock */ },
            onHistory = { currentScreen = "history" },
            onFinish = { currentScreen = "result" },
        )

        "history" -> HistoryScreen(
            events = listOf(
                ShotUi(28, "Коля", true, "21:34"),
                ShotUi(27, "Коля", true, "21:32"),
                ShotUi(26, "Коля", true, "21:31"),
                ShotUi(25, "Петя", false, "21:28"),
                ShotUi(24, "Петя", true, "21:24"),
            ),
            onBack = { currentScreen = "game" },
        )

        "result" -> ResultScreen(
            players = listOf(
                PlayerResultUi(1, "Саша", 12),
                PlayerResultUi(2, "Коля", 4),
                PlayerResultUi(3, "Петя", -5),
                PlayerResultUi(4, "Дима", -11),
            ),
            onNewGame = { currentScreen = "new_game" },
            onHome = { currentScreen = "home" },
        )
    }
}
