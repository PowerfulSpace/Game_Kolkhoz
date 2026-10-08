package ru.kolkhoz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kolkhoz.data.db.entity.PlayerEntity

/**
 * DAO игроков партии.
 */
@Dao
interface PlayerDao {

    /** Вставить игроков (replace при конфликте составного ключа). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayerEntity>)

    /**
     * Игроки партии в порядке `Game.players` (по listOrder).
     *
     * Сортировка именно по listOrder, а не по position: цикл GameRules
     * строится по порядку списка, а position — только метаданные.
     */
    @Query("SELECT * FROM players WHERE gameId = :gameId ORDER BY listOrder")
    suspend fun getByGameId(gameId: String): List<PlayerEntity>

    /** Удалить всех игроков партии. */
    @Query("DELETE FROM players WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: String)
}
