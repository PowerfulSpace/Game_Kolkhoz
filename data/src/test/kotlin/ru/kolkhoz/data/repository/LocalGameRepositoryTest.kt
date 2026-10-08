package ru.kolkhoz.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.kolkhoz.data.db.KolkhozDatabase
import ru.kolkhoz.domain.model.EventId
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.domain.model.ShotResult
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты [LocalGameRepository] на Room in-memory (Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
class LocalGameRepositoryTest {

    private lateinit var db: KolkhozDatabase
    private lateinit var repository: LocalGameRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KolkhozDatabase::class.java,
        ).build()
        repository = LocalGameRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun defaultPlayers() = listOf(
        Player(PlayerId("p0"), "Саша", position = 0),
        Player(PlayerId("p1"), "Петя", position = 1),
        Player(PlayerId("p2"), "Коля", position = 2),
    )

    private fun domainGame(
        id: String = "g1",
        createdAtMillis: Long = 1_000L,
        status: GameStatus = GameStatus.ACTIVE,
        players: List<Player> = defaultPlayers(),
        events: List<GameEvent> = emptyList(),
    ) = Game(GameId(id), createdAtMillis, players, events, status)

    @Test
    fun `saveGame затем loadGame возвращают эквивалентную партию`() {
        runTest {
            val original = domainGame(
                events = listOf(
                    GameEvent(EventId("g1:1"), 1, PlayerId("p1"), ShotResult.POCKET, 100L),
                    GameEvent(EventId("g1:2"), 2, PlayerId("p1"), ShotResult.MISS, 200L),
                ),
            )

            repository.saveGame(original)
            val loaded = repository.loadGame(original.id)

            assertEquals(original, loaded)
        }
    }

    @Test
    fun `повторное saveGame обновляет партию`() {
        runTest {
            val original = domainGame(events = emptyList())
            repository.saveGame(original)

            val updated = original.copy(
                status = GameStatus.FINISHED,
                events = listOf(
                    GameEvent(EventId("g1:1"), 1, PlayerId("p0"), ShotResult.POCKET, 100L),
                ),
            )
            repository.saveGame(updated)

            val loaded = repository.loadGame(original.id)
            assertEquals(updated, loaded)
        }
    }

    @Test
    fun `loadGame несуществующей партии - null`() {
        runTest {
            assertNull(repository.loadGame(GameId("missing")))
        }
    }

    @Test
    fun `observeActiveGame эмитит активную партию`() {
        runTest {
            val active = domainGame(id = "active", createdAtMillis = 1_000L)
            val finished = domainGame(
                id = "finished",
                createdAtMillis = 2_000L,
                status = GameStatus.FINISHED,
            )
            repository.saveGame(active)
            repository.saveGame(finished)

            val observed = repository.observeActiveGame().first()

            assertEquals(active, observed)
        }
    }

    @Test
    fun `observeActiveGame без активных партий - null`() {
        runTest {
            assertNull(repository.observeActiveGame().first())
        }
    }

    @Test
    fun `observeAllGames сортирует по createdAtMillis DESC`() {
        runTest {
            repository.saveGame(domainGame(id = "g1", createdAtMillis = 1_000L))
            repository.saveGame(domainGame(id = "g2", createdAtMillis = 3_000L))
            repository.saveGame(domainGame(id = "g3", createdAtMillis = 2_000L))

            val ids = repository.observeAllGames().first().map { it.id.value }

            assertEquals(listOf("g2", "g3", "g1"), ids)
        }
    }

    @Test
    fun `deleteGame затем loadGame - null`() {
        runTest {
            val game = domainGame()
            repository.saveGame(game)

            repository.deleteGame(game.id)

            assertNull(repository.loadGame(game.id))
        }
    }

    @Test
    fun `две активных партии - observeActiveGame возвращает последнюю по createdAtMillis`() {
        runTest {
            repository.saveGame(domainGame(id = "older", createdAtMillis = 1_000L))
            repository.saveGame(domainGame(id = "newer", createdAtMillis = 2_000L))

            val observed = repository.observeActiveGame().first()

            assertEquals("newer", observed?.id?.value)
        }
    }

    @Test
    fun `round trip перемешанных позиций - порядок position и recompute сохранены`() {
        runTest {
            // Обязательный тест: порядок списка ≠ порядку position.
            val players = listOf(
                Player(PlayerId("p0"), "Саша", position = 2),
                Player(PlayerId("p1"), "Петя", position = 0),
                Player(PlayerId("p2"), "Коля", position = 1),
            )
            val original = domainGame(
                players = players,
                events = listOf(
                    GameEvent(EventId("g1:1"), 1, PlayerId("p1"), ShotResult.POCKET, 100L),
                    GameEvent(EventId("g1:2"), 2, PlayerId("p1"), ShotResult.MISS, 200L),
                ),
            )
            repository.saveGame(original)

            val loaded = requireNotNull(repository.loadGame(original.id)) {
                "saved game must be loadable"
            }

            // Порядок Game.players сохранён по listOrder.
            assertEquals(listOf("Саша", "Петя", "Коля"), loaded.players.map { it.name })
            // position — метаданные, сохранены буквально.
            assertEquals(listOf(2, 0, 1), loaded.players.map { it.position })
            // Правила видят идентичное состояние.
            assertEquals(GameRules.recompute(original), GameRules.recompute(loaded))
        }
    }
}
