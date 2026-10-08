package ru.kolkhoz.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.kolkhoz.data.db.KolkhozDatabase
import ru.kolkhoz.data.mapper.toDomain
import ru.kolkhoz.data.mapper.toEntities
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.repository.GameRepository

/**
 * Реализация [GameRepository] поверх Room.
 *
 * Только «сохрани/загрузи»: никакого управления состоянием —
 * UI-состояние живёт в ViewModel (Phase 4).
 *
 * loadGame/observe возвращают null при отсутствии — это нормально,
 * не ошибка.
 */
@Singleton
class LocalGameRepository @Inject constructor(
    private val database: KolkhozDatabase,
) : GameRepository {

    override suspend fun saveGame(game: Game) {
        val (gameEntity, players, events) = game.toEntities()
        database.gameDao().saveGameWithChildren(
            gameEntity,
            players,
            events,
            database.playerDao(),
            database.eventDao(),
        )
    }

    override suspend fun loadGame(id: GameId): Game? {
        val gameEntity = database.gameDao().getById(id.value) ?: return null
        val players = database.playerDao().getByGameId(id.value)
        val events = database.eventDao().getByGameId(id.value)
        return gameEntity.toDomain(players, events)
    }

    override fun observeActiveGame(): Flow<Game?> {
        return database.gameDao().observeActive().map { entity ->
            entity?.let {
                val players = database.playerDao().getByGameId(it.id)
                val events = database.eventDao().getByGameId(it.id)
                it.toDomain(players, events)
            }
        }
    }

    override fun observeAllGames(): Flow<List<Game>> {
        return database.gameDao().observeAll().map { entities ->
            entities.map { entity ->
                val players = database.playerDao().getByGameId(entity.id)
                val events = database.eventDao().getByGameId(entity.id)
                entity.toDomain(players, events)
            }
        }
    }

    override suspend fun deleteGame(id: GameId) {
        database.gameDao().deleteById(id.value)
    }
}
