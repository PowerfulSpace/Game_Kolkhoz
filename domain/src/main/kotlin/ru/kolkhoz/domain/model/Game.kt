package ru.kolkhoz.domain.model

/**
 * Идентификатор партии.
 *
 * `String` — тот же выбор, что и для [PlayerId]: поддерживает UUID на уровне
 * хранения и исключает арифметику над идентификатором.
 */
@JvmInline
value class GameId(val value: String)

/**
 * Сохраняемая сущность партии.
 *
 * Единственное хранимое состояние — игроки и список событий.
 * Счёт, текущий игрок, серия и т.п. — производные (см. [GameState]).
 *
 * @property id идентификатор партии
 * @property createdAtMillis момент создания; приходит извне (domain не читает часы)
 * @property players игроки в фиксированном порядке хода (3..8)
 * @property events события партии в порядке добавления
 * @property status статус партии
 */
data class Game(
    val id: GameId,
    val createdAtMillis: Long,
    val players: List<Player>,
    val events: List<GameEvent>,
    val status: GameStatus,
)
