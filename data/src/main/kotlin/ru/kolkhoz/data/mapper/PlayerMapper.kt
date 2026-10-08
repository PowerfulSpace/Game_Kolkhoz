package ru.kolkhoz.data.mapper

import ru.kolkhoz.data.db.entity.PlayerEntity
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.model.PlayerId

/**
 * Игрок domain → строка таблицы `players`.
 *
 * @param gameId партия-владелец (входит в составной ключ)
 * @param listOrder позиция игрока в `Game.players` (0..N-1);
 *   при загрузке по этому полю восстанавливается порядок списка
 */
fun Player.toEntity(gameId: GameId, listOrder: Int): PlayerEntity = PlayerEntity(
    id = id.value,
    gameId = gameId.value,
    name = name,
    position = position,
    listOrder = listOrder,
)

/**
 * Строка таблицы `players` → игрок domain.
 *
 * `position` восстанавливается из entity как есть; порядок игроков
 * в партии определяется не этим полем, а порядком строк из DAO
 * (ORDER BY listOrder).
 */
fun PlayerEntity.toDomain(): Player = Player(
    id = PlayerId(id),
    name = name,
    position = position,
)
