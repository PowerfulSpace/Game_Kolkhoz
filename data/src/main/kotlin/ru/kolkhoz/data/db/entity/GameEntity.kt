package ru.kolkhoz.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Строка таблицы `games` — партия.
 *
 * Хранится только «шапка» партии: игроки и события — в отдельных
 * таблицах с CASCADE-удалением.
 *
 * @property id идентификатор партии (domain `GameId.value`)
 * @property createdAtMillis момент создания; порядок для истории
 *   и для выбора «последней активной» партии
 * @property status "ACTIVE" / "FINISHED" (domain `GameStatus.name`)
 */
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val createdAtMillis: Long,
    val status: String, // "ACTIVE" / "FINISHED"
)
