package ru.kolkhoz.ui.model

import ru.kolkhoz.domain.model.PlayerId

/**
 * Игрок для отображения (UI-модель экрана игры).
 *
 * Производная от domain-моделей ([ru.kolkhoz.domain.model.Player] +
 * счёт из [ru.kolkhoz.domain.model.GameState]): счёт не хранится,
 * а пересчитывается маппером. Идентификатор остаётся domain-овым
 * [PlayerId] — value class без логики и Android-зависимостей.
 *
 * @property id Идентификатор игрока.
 * @property name Имя игрока.
 * @property score Текущий счёт.
 * @property isCurrent Ходит ли сейчас этот игрок.
 */
data class PlayerUi(
    val id: PlayerId,
    val name: String,
    val score: Int,
    val isCurrent: Boolean,
)
