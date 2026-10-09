package ru.kolkhoz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import ru.kolkhoz.ui.theme.KolhozColors
import ru.kolkhoz.ui.theme.KolhozRadius
import ru.kolkhoz.ui.theme.KolhozSpacing
import ru.kolkhoz.ui.theme.KolkhozTheme
import ru.kolkhoz.ui.theme.KolhozTypography

/**
 * Диалог подтверждения (DESIGN_SYSTEM.md, раздел 9.7,
 * UI_SPEC.md, раздел 8).
 *
 * Радиус [KolhozRadius.XL], фон [KolhozColors.SurfaceElevated].
 * Кнопки: `ОТМЕНА` (text, [KolhozColors.Primary]) слева,
 * `ЗАВЕРШИТЬ` (destructive, [KolhozColors.Negative]) справа.
 * Stateless.
 *
 * @param title Заголовок диалога.
 * @param text Текст-пояснение.
 * @param onConfirm Колбэк подтверждения.
 * @param onDismiss Колбэк отмены (тап вне диалога тоже).
 * @param confirmText Текст кнопки подтверждения.
 * @param cancelText Текст кнопки отмены.
 * @param modifier Модификатор.
 */
@Composable
fun KolhozDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "ЗАВЕРШИТЬ",
    cancelText: String = "ОТМЕНА",
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(KolhozRadius.XL))
                .background(KolhozColors.SurfaceElevated)
                .padding(KolhozSpacing.XXL),
        ) {
            Text(
                text = title,
                style = KolhozTypography.H2,
                color = KolhozColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(KolhozSpacing.M))
            Text(
                text = text,
                style = KolhozTypography.Body,
                color = KolhozColors.TextSecondary,
            )
            Spacer(modifier = Modifier.height(KolhozSpacing.XXL))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KolhozTextButton(
                    text = cancelText,
                    onClick = onDismiss,
                    textColor = KolhozColors.Primary,
                )
                KolhozTextButton(
                    text = confirmText,
                    onClick = onConfirm,
                    textColor = KolhozColors.Negative,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08110F)
@Composable
private fun KolhozDialogPreview() {
    KolkhozTheme {
        KolhozDialog(
            title = "ЗАВЕРШИТЬ ИГРУ?",
            text = "Текущая партия будет сохранена в истории.",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
