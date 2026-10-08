package ru.kolkhoz.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Строка таблицы `game_events` — один удар партии.
 *
 * ID события в domain = `"gameId:sequenceNumber"` — глобально уникален,
 * поэтому primary key — один `id`.
 *
 * Удаление партии каскадно удаляет её события.
 *
 * @property id идентификатор события (domain `EventId.value`)
 * @property gameId партия, к которой относится удар
 * @property sequenceNumber порядковый номер удара (для сортировки)
 * @property playerId кто сделал удар
 * @property result "POCKET" / "MISS" (domain `ShotResult.name`)
 * @property timestampMillis момент удара (приходит извне)
 */
@Entity(
    tableName = "game_events",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("gameId", "sequenceNumber")],
)
data class GameEventEntity(
    @PrimaryKey val id: String,
    val gameId: String,
    val sequenceNumber: Int,
    val playerId: String,
    val result: String, // "POCKET" / "MISS"
    val timestampMillis: Long,
)
