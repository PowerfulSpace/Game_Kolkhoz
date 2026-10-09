package ru.kolkhoz

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ru.kolkhoz.navigation.KolkhozNavHost
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolkhozTheme

/**
 * Единственная Activity приложения.
 *
 * Hilt-точка входа ([AndroidEntryPoint]); в setContent —
 * корневая тема и граф навигации ([KolkhozNavHost]).
 *
 * Ориентация фиксируется в portrait до setContent, чтобы
 * запуск в landscape сразу стартовал в portrait (UI_SPEC.md 1.2);
 * далее [KolkhozNavHost] держит portrait на статических
 * экранах и отпускает на Game/History.
 *
 * Splash-тема (`Theme.Kolkhoz.Splash`, тёмный windowBackground)
 * включается в манифесте и снимается здесь же до
 * [super.onCreate] — вариант A без библиотек.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Kolkhoz)
        super.onCreate(savedInstanceState)
        // PORTRAIT только при холодном старте (UI_SPEC.md 1.2).
        // При повороте (savedInstanceState != null) НЕ ставим
        // принудительно — иначе Game/History не смогут повернуться
        // (recreate вызовет onCreate снова и заблокирует landscape).
        // Статические экраны (Home, NewGame, Result, GameHistory)
        // лочатся своим LockPortrait в KolkhozNavHost.
        if (savedInstanceState == null) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContent {
            KolkhozTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KolhozColors.Background,
                ) {
                    KolkhozNavHost()
                }
            }
        }
    }
}
