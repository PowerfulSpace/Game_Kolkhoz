package ru.kolkhoz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Строка игрока в списке (DESIGN_SYSTEM.md, раздел 9.1).
 *
 * Высота 56dp. Имя — [KolhozTypography.BodyMedium], счёт —
 * [ScoreText] (Body + Bold). Слева — [NumberBadge] с номером
 * (у текущего игрока — выделенный). У лидера партии перед
 * именем — корона [KolkhozIcons.Crown]. Текущий игрок
 * подсвечен фоном [KolhozColors.SurfaceElevated] с границей
 * [KolhozColors.Primary]. Stateless.
 *
 * Лидер — игрок с максимальным счётом; при равенстве счётов
 * корона достаётся первому в списке.
 *
 * @param name Имя игрока.
 * @param score Счёт игрока.
 * @param isCurrent Текущий ли это игрок.
 * @param number Порядковый номер игрока для [NumberBadge].
 * @param isLeader Лидер партии (максимальный счёт); корона.
 * @param modifier Модификатор.
 */
@Composable
fun PlayerRow(
    name: String,
    score: Int,
    isCurrent: Boolean,
    number: Int,
    modifier: Modifier = Modifier,
    isLeader: Boolean = false,
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
        NumberBadge(number = number, isHighlighted = isCurrent)
        Spacer(modifier = Modifier.width(KolhozSpacing.M))
        if (isLeader) {
            Image(
                painter = KolkhozIcons.Crown,
                contentDescription = "Лидер",
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(KolhozSpacing.S))
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
    KolkhozTheme {
        Column(verticalArrangement = Arrangement.spacedBy(KolhozSpacing.S)) {
            PlayerRow(name = "Саша", score = 7, isCurrent = true, number = 1, isLeader = true)
            PlayerRow(name = "Петя", score = 2, isCurrent = false, number = 2)
            PlayerRow(name = "Коля", score = -4, isCurrent = false, number = 3)
            PlayerRow(name = "Дима", score = 0, isCurrent = false, number = 4)
        }
    }
}
