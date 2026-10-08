package ru.kolkhoz.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import ru.kolkhoz.R

/**
 * Централизованный набор иконок MVP (DESIGN_SYSTEM.md, раздел 10).
 *
 * Источник — Material Icons core (не Unicode-символы: те
 * рендерятся по-разному на разных устройствах). [Pocket] и
 * [Undo] — свои vector drawable (res/drawable/), потому что их
 * нет в core. Кубок — растровый ассет `R.drawable.trophy`
 * (см. ResultScreen в Phase 3b), здесь его нет, потому что
 * это не [ImageVector].
 */
object KolkhozIcons {
    /** Новая игра (+). */
    val NewGame: ImageVector = Icons.Filled.Add

    /** Продолжить игру (▶). */
    val Continue: ImageVector = Icons.Filled.PlayArrow

    /**
     * Отмена удара (↶) — свой drawable `ic_undo`,
     * цвет задаётся через tint.
     */
    val Undo: ImageVector
        @Composable
        get() = vectorResource(R.drawable.ic_undo)

    /** История (☰). */
    val History: ImageVector = Icons.Filled.Menu

    /** Меню «⋮» в топ-баре. */
    val Menu: ImageVector = Icons.Filled.MoreVert

    /** Закрыть / крестик (×). */
    val Close: ImageVector = Icons.Filled.Close

    /**
     * Забитый шар (●) — свой drawable `ic_pocket`,
     * цвет задаётся через tint.
     */
    val Pocket: ImageVector
        @Composable
        get() = vectorResource(R.drawable.ic_pocket)

    /** Промах (×), красный через tint. */
    val Miss: ImageVector = Icons.Filled.Close

    /** Корона победителя (звезда). */
    val Crown: ImageVector = Icons.Filled.Star

    /** На главную (домик). */
    val Home: ImageVector = Icons.Filled.Home

    /** Кнопка «назад» в топ-баре. */
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack

    /** Уменьшить (−) в счётчике игроков. */
    val Decrease: ImageVector = Icons.Filled.Remove

    /** Увеличить (+) в счётчике игроков. */
    val Increase: ImageVector = Icons.Filled.Add
}
