package ru.kolkhoz.data.mapper

import ru.kolkhoz.data.db.entity.GameEventEntity
import ru.kolkhoz.domain.model.EventId
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.domain.model.ShotResult

/**
 * Событие domain → строка таблицы `game_events`.
 *
 * @param gameId партия-владелец (внешний ключ; в id события уже есть)
 */
fun GameEvent.toEntity(gameId: GameId): GameEventEntity = GameEventEntity(
    id = id.value,
    gameId = gameId.value,
    sequenceNumber = sequenceNumber,
    playerId = playerId.value,
    result = result.toRaw(),
    timestampMillis = timestampMillis,
)

/**
 * Строка таблицы `game_events` → событие domain.
 *
 * Неизвестное значение `result` — повреждённые данные, ошибка.
 */
fun GameEventEntity.toDomain(): GameEvent = GameEvent(
    id = EventId(id),
    sequenceNumber = sequenceNumber,
    playerId = PlayerId(playerId),
    result = result.toShotResult(),
    timestampMillis = timestampMillis,
)

/** Результат удара → строка для БД. */
private fun ShotResult.toRaw(): String = name

/** Строка из БД → результат удара. При неизвестном значении — ошибка. */
private fun String.toShotResult(): ShotResult =
    ShotResult.entries.firstOrNull { it.name == this }
        ?: throw IllegalArgumentException("Unknown ShotResult value in storage: \"$this\"")
