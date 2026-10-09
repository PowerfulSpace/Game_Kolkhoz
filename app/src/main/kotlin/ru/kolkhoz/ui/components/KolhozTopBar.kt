package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Верхняя панель (DESIGN_SYSTEM.md, раздел 9.8, UI_SPEC.md).
 *
 * Высота 56dp, без стандартного AppBar. Слева — заголовок
 * ([KolhozTypography.BodyMedium]; по умолчанию «КОЛХОЗ»),
 * опционально — кнопка «назад». Справа — иконка «⋮»
 * (48dp touch target), если задан [onMenuClick]. Stateless.
 *
 * @param title Заголовок слева.
 * @param onMenuClick Колбэк меню «⋮»; `null` — иконка скрыта.
 * @param onBackClick Колбэк «назад»; `null` — кнопка скрыта.
 * @param modifier Модификатор.
 */
@Composable
fun KolhozTopBar(
    title: String = "КОЛХОЗ",
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(KolhozColors.Background)
            .padding(horizontal = KolhozSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = KolkhozIcons.Back,
                    contentDescription = "Назад",
                    tint = KolhozColors.TextPrimary,
                )
            }
        }
        Text(
            text = title,
            style = KolhozTypography.BodyMedium,
            color = KolhozColors.TextPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(start = KolhozSpacing.S),
        )
        if (onMenuClick != null) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = KolkhozIcons.Menu,
                    contentDescription = "Меню",
                    tint = KolhozColors.TextPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun KolhozTopBarPreview() {
    KolkhozTheme {
        Column {
            KolhozTopBar(title = "КОЛХОЗ", onMenuClick = {})
            KolhozTopBar(title = "НОВАЯ ИГРА", onBackClick = {})
        }
    }
}
