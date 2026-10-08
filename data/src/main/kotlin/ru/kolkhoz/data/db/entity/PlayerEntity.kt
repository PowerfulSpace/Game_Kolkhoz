package ru.kolkhoz.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Строка таблицы `players` — игрок внутри партии.
 *
 * ВАЖНО: id игрока уникален ВНУТРИ партии (domain так определяет),
 * поэтому primary key — составной `(id, gameId)`.
 *
 * Удаление партии каскадно удаляет её игроков.
 *
 * @property id идентификатор игрока (domain `PlayerId.value`)
 * @property gameId партия, к которой принадлежит игрок
 * @property name имя игрока (как ввёл пользователь)
 * @property position метаданные из domain (`Player.position`);
 *   в правилах игры не участвует, хранится, чтобы round-trip был
 *   буквальным
 * @property listOrder порядок игрока в `Game.players` (0..N-1);
 *   по нему сортируется загрузка — цикл GameRules строится по
 *   порядку списка, а не по position
 */
@Entity(
    tableName = "players",
    primaryKeys = ["id", "gameId"],
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("gameId")],
)
data class PlayerEntity(
    val id: String,
    val gameId: String,
    val name: String,
    val position: Int,
    val listOrder: Int,
)
