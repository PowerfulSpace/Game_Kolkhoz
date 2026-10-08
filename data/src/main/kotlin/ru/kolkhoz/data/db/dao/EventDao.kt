package ru.kolkhoz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kolkhoz.data.db.entity.GameEventEntity

/**
 * DAO событий (ударов) партии.
 */
@Dao
interface EventDao {

    /** Вставить события (replace при конфликте id). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<GameEventEntity>)

    /** События партии по порядку sequenceNumber. */
    @Query("SELECT * FROM game_events WHERE gameId = :gameId ORDER BY sequenceNumber")
    suspend fun getByGameId(gameId: String): List<GameEventEntity>

    /** Удалить все события партии. */
    @Query("DELETE FROM game_events WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: String)
}
