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
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KolkhozTheme {
                KolkhozNavHost()
            }
        }
    }
}
