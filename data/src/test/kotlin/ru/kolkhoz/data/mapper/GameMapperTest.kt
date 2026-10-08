package ru.kolkhoz.data.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kolkhoz.data.db.entity.GameEntity
import ru.kolkhoz.domain.model.EventId
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.domain.model.ShotResult

/**
 * Тесты [GameMapper]: партия ↔ набор строк БД.
 */
class GameMapperTest {

    /** Партия с намеренно «перемешанными» позициями: порядок списка ≠ порядку position. */
    private fun sampleGame(status: GameStatus = GameStatus.ACTIVE) = Game(
        id = GameId("game-1"),
        createdAtMillis = 5_000L,
        players = listOf(
            Player(PlayerId("p0"), "Саша", position = 2),
            Player(PlayerId("p1"), "Петя", position = 0),
            Player(PlayerId("p2"), "Коля", position = 1),
        ),
        events = listOf(
            GameEvent(EventId("game-1:1"), 1, PlayerId("p1"), ShotResult.MISS, 1_000L),
            GameEvent(EventId("game-1:2"), 2, PlayerId("p1"), ShotResult.POCKET, 2_000L),
        ),
        status = status,
    )

    @Test
    fun `toEntities разбивает партию на шапку игроков и события`() {
        val (gameEntity, players, events) = sampleGame().toEntities()

        assertEquals("game-1", gameEntity.id)
        assertEquals(5_000L, gameEntity.createdAtMillis)
        assertEquals("ACTIVE", gameEntity.status)

        assertEquals(3, players.size)
        // listOrder — индекс в Game.players: 0,1,2 в порядке списка.
        assertEquals(listOf(0, 1, 2), players.map { it.listOrder })
        assertEquals(listOf(2, 0, 1), players.map { it.position })
        assertEquals(listOf("Саша", "Петя", "Коля"), players.map { it.name })
        assertTrue(players.all { it.gameId == "game-1" })

        assertEquals(2, events.size)
        assertEquals(listOf(1, 2), events.map { it.sequenceNumber })
        assertTrue(events.all { it.gameId == "game-1" })
    }

    @Test
    fun `round trip сохраняет партию`() {
        val original = sampleGame()

        val (gameEntity, players, events) = original.toEntities()
        val restored = gameEntity.toDomain(players, events)

        assertEquals(original, restored)
    }

    @Test
    fun `round trip сохраняет порядок списка и перемешанные позиции`() {
        val original = sampleGame()
        val (gameEntity, players, events) = original.toEntities()

        // Строки приходят из DAO уже в порядке listOrder — как в PlayerDao.getByGameId.
        val restored = gameEntity.toDomain(
            players.sortedBy { it.listOrder },
            events.sortedBy { it.sequenceNumber },
        )

        assertEquals(listOf("Саша", "Петя", "Коля"), restored.players.map { it.name })
        assertEquals(listOf(2, 0, 1), restored.players.map { it.position })
        assertEquals(original.players, restored.players)
    }

    @Test
    fun `status маппится в строку и обратно`() {
        assertEquals("ACTIVE", sampleGame(GameStatus.ACTIVE).toEntities().first.status)
        assertEquals("FINISHED", sampleGame(GameStatus.FINISHED).toEntities().first.status)

        assertEquals(
            GameStatus.FINISHED,
            GameEntity("game-1", 1L, "FINISHED").toDomain(emptyList(), emptyList()).status,
        )
    }

    @Test
    fun `неизвестный status - ошибка`() {
        val corrupted = GameEntity("game-1", 1L, "PAUSED")

        assertThrows(IllegalArgumentException::class.java) {
            corrupted.toDomain(emptyList(), emptyList())
        }
    }
}
