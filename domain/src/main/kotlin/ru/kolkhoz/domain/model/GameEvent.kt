package ru.kolkhoz.domain.model

/**
 * Идентификатор события (удара).
 *
 * Как и [PlayerId], — `String`: естественно сериализуется и допускает
 * производные идентификаторы вида `"gameId:sequenceNumber"`.
 */
@JvmInline
value class EventId(val value: String)

/**
 * Одно событие партии — удар конкретного игрока.
 *
 * События неизменяемы и образуют единственный источник истины:
 * счёт и все остальные производные поля [GameState] вычисляются из списка событий.
 *
 * @property id идентификатор события
 * @property sequenceNumber порядковый номер удара в партии, начинается с 1
 * @property playerId кто сделал удар
 * @property result исход удара
 * @property timestampMillis момент удара; приходит извне — domain никогда не читает часы сам
 */
data class GameEvent(
    val id: EventId,
    val sequenceNumber: Int,
    val playerId: PlayerId,
    val result: ShotResult,
    val timestampMillis: Long,
)
