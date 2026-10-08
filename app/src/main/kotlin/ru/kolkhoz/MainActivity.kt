package ru.kolkhoz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import ru.kolkhoz.navigation.KolkhozNavHost
import ru.kolkhoz.ui.theme.KolkhozTheme

/**
 * Единственная Activity приложения.
 *
 * Hilt-точка входа ([AndroidEntryPoint]); в setContent —
 * корневая тема и граф навигации ([KolkhozNavHost]).
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
        setContent {
            KolkhozTheme {
                KolkhozNavHost()
            }
        }
    }
}
