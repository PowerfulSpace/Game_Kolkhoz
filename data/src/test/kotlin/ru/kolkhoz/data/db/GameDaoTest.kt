package ru.kolkhoz.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.kolkhoz.data.db.dao.EventDao
import ru.kolkhoz.data.db.dao.GameDao
import ru.kolkhoz.data.db.entity.GameEntity
import ru.kolkhoz.data.db.entity.GameEventEntity

/**
 * Тесты [GameDao] на Room in-memory (Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
class GameDaoTest {

    private lateinit var db: KolkhozDatabase
    private lateinit var gameDao: GameDao
    private lateinit var eventDao: EventDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KolkhozDatabase::class.java,
        ).build()
        gameDao = db.gameDao()
        eventDao = db.eventDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun game(id: String, createdAt: Long, status: String = "ACTIVE") =
        GameEntity(id, createdAt, status)

    private fun event(id: String, gameId: String, sequenceNumber: Int = 1) =
        GameEventEntity(id, gameId, sequenceNumber, "p0", "POCKET", 1_000L)

    @Test
    fun `upsert и getById возвращают партию`() {
        runTest {
            gameDao.upsert(game("g1", 1_000L))

            assertEquals(GameEntity("g1", 1_000L, "ACTIVE"), gameDao.getById("g1"))
        }
    }

    @Test
    fun `getById несуществующей партии - null`() {
        runTest {
            assertNull(gameDao.getById("missing"))
        }
    }

    @Test
    fun `observeActive возвращает последнюю активную по createdAtMillis`() {
        runTest {
            gameDao.upsert(game("old", 1_000L))
            gameDao.upsert(game("new", 2_000L))
            gameDao.upsert(game("finished", 3_000L, status = "FINISHED"))

            val active = gameDao.observeActive().first()

            assertEquals("new", active?.id)
        }
    }

    @Test
    fun `observeAll сортирует по createdAtMillis DESC`() {
        runTest {
            gameDao.upsert(game("g1", 1_000L))
            gameDao.upsert(game("g2", 3_000L))
            gameDao.upsert(game("g3", 2_000L))

            val ids = gameDao.observeAll().first().map { it.id }

            assertEquals(listOf("g2", "g3", "g1"), ids)
        }
    }

    @Test
    fun `deleteById удаляет партию`() {
        runTest {
            gameDao.upsert(game("g1", 1_000L))

            gameDao.deleteById("g1")

            assertNull(gameDao.getById("g1"))
        }
    }

    @Test
    fun `удаление партии каскадно удаляет события`() {
        runTest {
            gameDao.upsert(game("g1", 1_000L))
            eventDao.insertAll(listOf(event("g1:1", "g1")))

            gameDao.deleteById("g1")

            assertTrue(eventDao.getByGameId("g1").isEmpty())
        }
    }
}
