package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Поле ввода имени игрока (DESIGN_SYSTEM.md, раздел 9.10).
 *
 * Высота 56dp, радиус [KolhozRadius.M], фон [KolhozColors.Surface],
 * граница 1dp: [KolhozColors.Border], в фокусе — [KolhozColors.Primary],
 * при ошибке — [KolhozColors.Negative]. Placeholder «Имя игрока».
 * Слева — номер игрока (Caption), справа — крестик × (удалить,
 * 48dp touch target). Длина ограничена [maxLength]. Stateless.
 *
 * @param value Текущее значение поля.
 * @param onValueChange Колбэк изменения значения (уже обрезано
 *   до [maxLength]).
 * @param playerNumber Порядковый номер игрока (с единицы).
 * @param onDelete Колбэк удаления игрока (крестик).
 * @param maxLength Максимальная длина имени.
 * @param errorText Текст ошибки; `null` — ошибки нет.
 * @param modifier Модификатор.
 */
@Composable
fun PlayerInput(
    value: String,
    onValueChange: (String) -> Unit,
    playerNumber: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 32,
    errorText: String? = null,
) {
    var focused by remember { mutableStateOf(false) }

    val borderColor = when {
        errorText != null -> KolhozColors.Negative
        focused -> KolhozColors.Primary
        else -> KolhozColors.Border
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(KolhozRadius.M))
                .background(KolhozColors.Surface)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(KolhozRadius.M),
                )
                .padding(start = KolhozSpacing.M, end = KolhozSpacing.XS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = playerNumber.toString(),
                style = KolhozTypography.Caption,
                color = KolhozColors.TextSecondary,
                modifier = Modifier
                    .width(24.dp)
                    .padding(end = KolhozSpacing.S),
            )
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = "Имя игрока",
                        style = KolhozTypography.BodyMedium,
                        color = KolhozColors.TextDisabled,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(it.take(maxLength)) },
                    textStyle = KolhozTypography.BodyMedium.copy(
                        color = KolhozColors.TextPrimary,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(KolhozColors.Primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = KolkhozIcons.Close,
                    contentDescription = "Удалить игрока",
                    tint = KolhozColors.TextSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = KolhozTypography.Caption,
                color = KolhozColors.Negative,
                modifier = Modifier.padding(
                    start = KolhozSpacing.M,
                    top = KolhozSpacing.XS,
                ),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun PlayerInputPreview() {
    KolkhozTheme {
        Column(
            modifier = Modifier.padding(KolhozSpacing.L),
            verticalArrangement = Arrangement.spacedBy(KolhozSpacing.M),
        ) {
            var name by remember { mutableStateOf("Саша") }
            PlayerInput(
                value = name,
                onValueChange = { name = it },
                playerNumber = 1,
                onDelete = {},
            )
            PlayerInput(
                value = "",
                onValueChange = {},
                playerNumber = 2,
                onDelete = {},
                errorText = "Введите имя игрока",
            )
        }
    }
}
