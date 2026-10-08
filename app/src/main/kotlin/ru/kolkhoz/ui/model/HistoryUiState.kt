package ru.kolkhoz.ui.model

/**
 * Состояние экрана истории (UI_SPEC.md, раздел 5).
 *
 * @property events Удары, отсортированные свежими вперёд.
 * @property isEmpty Пуста ли история (партия только создана) —
 *   экран показывает «Пока нет ударов».
 */
data class HistoryUiState(
    val events: List<ShotUi>,
    val isEmpty: Boolean,
)
