package ru.kolkhoz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.kolkhoz.data.db.dao.EventDao
import ru.kolkhoz.data.db.dao.GameDao
import ru.kolkhoz.data.db.dao.PlayerDao
import ru.kolkhoz.data.db.entity.GameEntity
import ru.kolkhoz.data.db.entity.GameEventEntity
import ru.kolkhoz.data.db.entity.PlayerEntity

/**
 * Room-база «Колхоза».
 *
 * Версия 1 — миграций пока нет: новые схемы ломают данные осознанно
 * только на этапе разработки.
 */
@Database(
    entities = [GameEntity::class, PlayerEntity::class, GameEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class KolkhozDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun eventDao(): EventDao
}
