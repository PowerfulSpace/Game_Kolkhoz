package ru.kolkhoz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kolkhoz.R
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Верхняя панель (DESIGN_SYSTEM.md, раздел 9.8, UI_SPEC.md).
 *
 * Высота 56dp, без стандартного AppBar. Заголовок — текст
 * ([title]) или лого-картинка ([logoRes]) — ПО ЦЕНТРУ панели.
 * Слева — кнопка «назад» (48dp), если задан [onBackClick].
 * Справа — иконка «⋮» (48dp), если задан [onMenuClick].
 * Снизу — линия-разделитель 1dp ([showDivider]). Stateless.
 *
 * @param title Текстовый заголовок (когда [logoRes] == null).
 * @param logoRes Drawable-ресурс лого вместо текста
 *   (например `R.drawable.logo_topbar`).
 * @param onMenuClick Колбэк меню «⋮»; `null` — иконка скрыта.
 * @param onBackClick Колбэк «назад»; `null` — кнопка скрыта.
 * @param showDivider Рисовать ли линию-разделитель снизу.
 * @param modifier Модификатор.
 */
@Composable
fun KolhozTopBar(
    title: String? = null,
    logoRes: Int? = null,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
) {
    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(KolhozColors.Background),
        ) {
            // Заголовок (текст или лого) — по центру.
            if (logoRes != null) {
                Image(
                    painter = painterResource(logoRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .height(40.dp),
                )
            } else if (title != null) {
                Text(
                    text = title,
                    style = KolhozTypography.BodyMedium,
                    color = KolhozColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = KolhozSpacing.XXXXXL),
                )
            }

            // Back — слева.
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp),
                ) {
                    Icon(
                        imageVector = KolkhozIcons.Back,
                        contentDescription = "Назад",
                        tint = KolhozColors.TextPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // ⋮ — справа.
            if (onMenuClick != null) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(48.dp),
                ) {
                    Icon(
                        imageVector = KolkhozIcons.Menu,
                        contentDescription = "Меню",
                        tint = KolhozColors.TextPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = KolhozColors.Border,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun KolhozTopBarPreview() {
    KolkhozTheme {
        Column {
            KolhozTopBar(title = "НОВАЯ ИГРА", onBackClick = {})
            KolhozTopBar(title = "ИСТОРИЯ ИГРЫ", onBackClick = {})
            KolhozTopBar(logoRes = R.drawable.logo_topbar, onMenuClick = {})
        }
    }
}
