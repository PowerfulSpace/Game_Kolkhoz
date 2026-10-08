package ru.kolkhoz.data.mapper

import ru.kolkhoz.data.db.entity.GameEntity
import ru.kolkhoz.data.db.entity.GameEventEntity
import ru.kolkhoz.data.db.entity.PlayerEntity
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.GameStatus

/**
 * Партия domain → набор строк БД: шапка + игроки + события.
 *
 * `listOrder` игроков — это индекс в `Game.players`, чтобы при
 * загрузке восстановился ровно тот же порядок списка (по нему
 * строится цикл GameRules).
 */
fun Game.toEntities(): Triple<GameEntity, List<PlayerEntity>, List<GameEventEntity>> =
    Triple(
        GameEntity(
            id = id.value,
            createdAtMillis = createdAtMillis,
            status = status.toRaw(),
        ),
        players.mapIndexed { index, player -> player.toEntity(id, index) },
        events.map { it.toEntity(id) },
    )

/**
 * Строки БД → партия domain.
 *
 * @param players строки игроки **уже в порядке `listOrder`**
 *   (см. `PlayerDao.getByGameId`)
 * @param events строки событий **уже в порядке `sequenceNumber`**
 *   (см. `EventDao.getByGameId`)
 *
 * Неизвестное значение `status` — повреждённые данные, ошибка.
 */
fun GameEntity.toDomain(
    players: List<PlayerEntity>,
    events: List<GameEventEntity>,
): Game = Game(
    id = GameId(id),
    createdAtMillis = createdAtMillis,
    players = players.map { it.toDomain() },
    events = events.map { it.toDomain() },
    status = status.toGameStatus(),
)

/** Статус партии → строка для БД. */
private fun GameStatus.toRaw(): String = name

/** Строка из БД → статус партии. При неизвестном значении — ошибка. */
private fun String.toGameStatus(): GameStatus =
    GameStatus.entries.firstOrNull { it.name == this }
        ?: throw IllegalArgumentException("Unknown GameStatus value in storage: \"$this\"")
