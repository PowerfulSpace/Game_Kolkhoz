package ru.kolkhoz.ui.model

/**
 * Состояние экрана результатов (UI_SPEC.md, раздел 6).
 *
 * @property players Игроки с местами, отсортированные по счёту
 *   убывание; при равных счетах — в порядке списка игроков.
 * @property isLoading Идёт ли загрузка партии.
 * @property errorMessage Человеческое сообщение об ошибке;
 *   `null` — ошибки нет.
 */
data class ResultUiState(
    val players: List<PlayerResultUi>,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
