package ru.kolkhoz.ui.model

/**
 * Состояние главного экрана (UI_SPEC.md, раздел 2).
 *
 * @property hasActiveGame Есть ли активная партия — показывает
 *   кнопку «ПРОДОЛЖИТЬ ИГРУ».
 * @property isLoading Идёт ли загрузка (первичный emission).
 * @property errorMessage Человеческое сообщение об ошибке
 *   загрузки; `null` — ошибки нет.
 */
data class HomeUiState(
    val hasActiveGame: Boolean,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
