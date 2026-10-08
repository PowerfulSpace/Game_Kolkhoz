package ru.kolkhoz.ui.model

/**
 * Игрок в итоговой таблице (UI-модель экрана результатов).
 *
 * @property place Место (1, 2, 3, ...), по убыванию счёта.
 * @property name Имя игрока.
 * @property score Итоговый счёт.
 */
data class PlayerResultUi(
    val place: Int,
    val name: String,
    val score: Int,
)
