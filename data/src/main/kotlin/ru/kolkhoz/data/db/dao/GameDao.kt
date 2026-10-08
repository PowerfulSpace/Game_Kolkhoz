package ru.kolkhoz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.kolkhoz.data.db.entity.GameEntity
import ru.kolkhoz.data.db.entity.GameEventEntity
import ru.kolkhoz.data.db.entity.PlayerEntity

/**
 * DAO партий: шапка игры плюс транзакционное сохранение с детьми.
 */
@Dao
interface GameDao {

    /** Создать или обновить партию. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(game: GameEntity)

    /** Партия по id; null — если не найдена. */
    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: String): GameEntity?

    /**
     * Последняя активная партия.
     *
     * Если активных несколько (баг, миграция) — берётся самая свежая
     * по createdAtMillis.
     */
    @Query("SELECT * FROM games WHERE status = 'ACTIVE' ORDER BY createdAtMillis DESC LIMIT 1")
    fun observeActive(): Flow<GameEntity?>

    /** Все партии, свежие — первыми. */
    @Query("SELECT * FROM games ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<GameEntity>>

    /** Удалить партию; игроки и события удаляются каскадом. */
    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Транзакционное сохранение всей партии: шапка + игроки + события.
     *
     * Дочерние таблицы сначала чистятся от старой версии партии,
     * затем записывается новая — так saveGame работает и как вставка,
     * и как обновление.
     */
    @Transaction
    suspend fun saveGameWithChildren(
        game: GameEntity,
        players: List<PlayerEntity>,
        events: List<GameEventEntity>,
        playerDao: PlayerDao,
        eventDao: EventDao,
    ) {
        upsert(game)
        playerDao.deleteByGameId(game.id)
        playerDao.insertAll(players)
        eventDao.deleteByGameId(game.id)
        eventDao.insertAll(events)
    }
}
