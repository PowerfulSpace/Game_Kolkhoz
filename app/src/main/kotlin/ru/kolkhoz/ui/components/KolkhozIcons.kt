package ru.kolkhoz.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import ru.kolkhoz.R

/**
 * Централизованный набор иконок MVP (DESIGN_SYSTEM.md, раздел 10).
 *
 * Гибрид двух типов:
 * - [ImageVector] — Material Icons core и vector drawable
 *   (для `Icon(imageVector = ...)`);
 * - [Painter] — PNG drawable (для `Icon(painter = ...)`),
 *   потому что `vectorResource` не работает с растровыми PNG.
 */
object KolkhozIcons {
    // === Material Icons (ImageVector) ===

    /** Новая игра (+). */
    val NewGame: ImageVector = Icons.Filled.Add

    /** Продолжить игру (▶). */
    val Continue: ImageVector = Icons.Filled.PlayArrow

    /** Меню «⋮» в топ-баре. */
    val Menu: ImageVector = Icons.Filled.MoreVert

    /** Закрыть / крестик (×). */
    val Close: ImageVector = Icons.Filled.Close

    /** Кнопка «назад» в топ-баре. */
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack

    /** Увеличить (+) в счётчике игроков. */
    val Increase: ImageVector = Icons.Filled.Add

    // === Vector drawable (ImageVector) ===

    /**
     * Отмена удара (↶) — свой drawable `ic_undo`,
     * цвет задаётся через tint.
     */
    val Undo: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.ic_undo)

    /**
     * Уменьшить (−) в счётчике игроков — свой drawable
     * `ic_remove` (нет в material-icons-core), цвет через tint.
     */
    val Decrease: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.ic_remove)

    // === PNG drawable (Painter) ===

    /** Забитый шар (●) — растровый `ic_pocket`. */
    val Pocket: Painter
        @Composable
        get() = painterResource(R.drawable.ic_pocket)

    /** История — растровый `ic_history`. */
    val History: Painter
        @Composable
        get() = painterResource(R.drawable.ic_history)

    /** Корона победителя — растровый `ic_crown`. */
    val Crown: Painter
        @Composable
        get() = painterResource(R.drawable.ic_crown)

    /** Обновление / новая игра — растровый `ic_refresh`. */
    val Refresh: Painter
        @Composable
        get() = painterResource(R.drawable.ic_refresh)

    /** На главную (домик) — растровый `ic_home`. */
    val Home: Painter
        @Composable
        get() = painterResource(R.drawable.ic_home)
}
