package ru.kolkhoz.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameId

/**
 * Контракт доступа к хранилищу партий.
 *
 * Реализации живут в data (LocalGameRepository). Domain не знает,
 * откуда данные — из Room, из сети, из памяти.
 *
 * Единственное место в domain, где допустимы suspend и Flow:
 * это контракт с внешним миром (persistence), а не правила игры.
 * GameRules остаётся чистой и синхронной.
 */
interface GameRepository {

    /** Сохранить партию (создать или обновить). */
    suspend fun saveGame(game: Game)

    /** Загрузить партию по id. null — если не найдена. */
    suspend fun loadGame(id: GameId): Game?

    /**
     * Наблюдать за активной партией.
     *
     * Если активных партий несколько (баг, миграция) — возвращает
     * последнюю по createdAtMillis.
     * Если активной нет — null.
     */
    fun observeActiveGame(): Flow<Game?>

    /** Наблюдать за всеми партиями (для истории). Свежие — первыми. */
    fun observeAllGames(): Flow<List<Game>>

    /** Удалить партию по id. */
    suspend fun deleteGame(id: GameId)
}
