package ru.kolkhoz.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Центрирует контент по горизонтали с ограничением ширины.
 *
 * В portrait контент занимает всю ширину (maxWidth = 480dp —
 * больше любого телефона). В landscape контент центрируется,
 * по бокам остаются тёмные поля.
 *
 * Используется на статических экранах (Home, NewGame, Result,
 * GameHistory), чтобы в landscape они выглядели как portrait.
 */
@Composable
fun CenteredContent(
    modifier: Modifier = Modifier,
    maxWidthDp: Int = 480,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Порядок важен: widthIn сначала ограничивает max,
        // fillMaxSize заполняет уже ограниченную ширину.
        Box(
            modifier = Modifier
                .widthIn(max = maxWidthDp.dp)
                .fillMaxSize(),
        ) {
            content()
        }
    }
}
