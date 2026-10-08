package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Строка игрока в списке (DESIGN_SYSTEM.md, раздел 9.1).
 *
 * Высота 56dp. Имя — [KolhozTypography.BodyMedium], счёт —
 * [ScoreText] (Body + Bold). Слева — индикатор ●
 * ([KolhozColors.Primary]), только если это текущий игрок;
 * тогда фон [KolhozColors.SurfaceElevated] с границей
 * [KolhozColors.Primary]. Stateless.
 *
 * @param name Имя игрока.
 * @param score Счёт игрока.
 * @param isCurrent Текущий ли это игрок.
 * @param modifier Модификатор.
 */
@Composable
fun PlayerRow(
    name: String,
    score: Int,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    val background = if (isCurrent) {
        KolhozColors.SurfaceElevated
    } else {
        KolhozColors.Background
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(KolhozRadius.S))
            .background(background)
            .border(
                width = if (isCurrent) 1.dp else 0.dp,
                color = KolhozColors.Primary,
                shape = RoundedCornerShape(KolhozRadius.S),
            )
            .padding(horizontal = KolhozSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(KolhozColors.Primary),
            )
            Spacer(modifier = Modifier.size(KolhozSpacing.M))
        }
        Text(
            text = name,
            style = KolhozTypography.BodyMedium,
            color = KolhozColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        ScoreText(score = score, style = rowScoreStyle)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun PlayerRowPreview() {
    KolhozTheme {
        Column(
            modifier = Modifier.padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.S),
        ) {
            PlayerRow(name = "Саша", score = 7, isCurrent = true)
            PlayerRow(name = "Петя", score = 2, isCurrent = false)
            PlayerRow(name = "Коля", score = -4, isCurrent = false)
            PlayerRow(name = "Дима", score = 0, isCurrent = false)
        }
    }
}
