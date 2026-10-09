package ru.kolkhoz.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Отображение счёта (DESIGN_SYSTEM.md, раздел 9.3).
 *
 * Цвет: `> 0` — [KolhozColors.Positive], `< 0` —
 * [KolhozColors.Negative], `0` — [KolhozColors.TextSecondary].
 * Формат со знаком: `+7`, `−4`, `0` (минус — типографский).
 * Размер задаётся стилем: [KolhozTypography.Display] (крупный)
 * или `Body + Bold` (в списках). Stateless.
 *
 * При изменении счёта — всплеск `scale 1.0 → 1.12 → 1.0`,
 * 180ms (DESIGN_SYSTEM.md 11.2). `snapTo(1f)` в начале
 * эффекта отменяет предыдущую анимацию при быстрой серии
 * ударов (DESIGN_SYSTEM.md 11.4).
 *
 * @param score Счёт игрока (может быть отрицательным).
 * @param style Стиль текста (по умолчанию [KolhozTypography.Display]).
 * @param modifier Модификатор.
 */
@Composable
fun ScoreText(
    score: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = KolhozTypography.Display,
) {
    val color = when {
        score > 0 -> KolhozColors.Positive
        score < 0 -> KolhozColors.Negative
        else -> KolhozColors.TextSecondary
    }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(score) {
        scale.snapTo(1f)
        scale.animateTo(1.12f, tween(90))
        scale.animateTo(1f, tween(90))
    }
    Text(
        text = formatScore(score),
        style = style,
        color = color,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    )
}

/** Форматирует счёт со знаком: `+7`, `−4`, `0`. */
internal fun formatScore(score: Int): String = when {
    score > 0 -> "+$score"
    score < 0 -> "−${-score}"
    else -> "0"
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun ScoreTextPreview() {
    KolkhozTheme {
        Column(
            modifier = Modifier.padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.S),
        ) {
            ScoreText(score = 7)
            ScoreText(score = -4, style = rowScoreStyle)
            ScoreText(score = 0, style = rowScoreStyle)
        }
    }
}

/** Стиль счёта в строках списков: Body + Bold (DESIGN_SYSTEM.md 9.1). */
internal val rowScoreStyle: TextStyle
    get() = KolhozTypography.Body.copy(fontWeight = FontWeight.Bold)
