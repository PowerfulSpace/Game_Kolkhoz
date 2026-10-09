package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/** Максимум точек серии (DESIGN_SYSTEM.md, раздел 9.4). */
private const val MAX_SERIES_DOTS = 6

/**
 * Индикатор серии (DESIGN_SYSTEM.md, раздел 9.4).
 *
 * Показываем **только заполненные** точки: серия 4 — 4 точки.
 * Максимум [MAX_SERIES_DOTS]; при серии больше 6 — 6 точек и
 * текст `+N`. Подпись `СЕРИЯ N` — [KolhozTypography.Caption].
 * При серии `<= 0` ничего не рисуем. Stateless.
 *
 * @param series Текущая серия игрока.
 * @param modifier Модификатор.
 */
@Composable
fun SeriesIndicator(
    series: Int,
    modifier: Modifier = Modifier,
) {
    if (series <= 0) return

    val dots = minOf(series, MAX_SERIES_DOTS)
    val overflow = series - MAX_SERIES_DOTS

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KolhozSpacing.XS),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(KolhozSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(dots) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(KolhozColors.Primary),
                )
            }
            if (overflow > 0) {
                Text(
                    text = "+$overflow",
                    style = KolhozTypography.BodyMedium,
                    color = KolhozColors.Primary,
                )
            }
        }
        Text(
            text = "СЕРИЯ $series",
            style = KolhozTypography.Caption,
            color = KolhozColors.TextSecondary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun SeriesIndicatorPreview() {
    KolkhozTheme {
        Column(
            modifier = Modifier.padding(KolhozSpacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.XL),
        ) {
            SeriesIndicator(series = 4)
            SeriesIndicator(series = 7)
        }
    }
}
