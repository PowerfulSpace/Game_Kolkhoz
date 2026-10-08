package ru.kolkhoz.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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
 * Тесты [EventDao] на Room in-memory (Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
class EventDaoTest {

    private lateinit var db: KolkhozDatabase
    private lateinit var eventDao: EventDao
    private lateinit var gameDao: GameDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KolkhozDatabase::class.java,
        ).build()
        eventDao = db.eventDao()
        gameDao = db.gameDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun event(id: String, gameId: String, sequenceNumber: Int, result: String = "POCKET") =
        GameEventEntity(id, gameId, sequenceNumber, "p0", result, 1_000L)

    @Test
    fun `getByGameId сортирует по sequenceNumber`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            eventDao.insertAll(
                listOf(
                    event("g1:3", "g1", 3),
                    event("g1:1", "g1", 1),
                    event("g1:2", "g1", 2, result = "MISS"),
                ),
            )

            val loaded = eventDao.getByGameId("g1")

            assertEquals(listOf(1, 2, 3), loaded.map { it.sequenceNumber })
            assertEquals(listOf("POCKET", "MISS", "POCKET"), loaded.map { it.result })
        }
    }

    @Test
    fun `getByGameId возвращает только события своей партии`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            gameDao.upsert(GameEntity("g2", 2_000L, "ACTIVE"))
            eventDao.insertAll(
                listOf(
                    event("g1:1", "g1", 1),
                    event("g2:1", "g2", 1),
                    event("g2:2", "g2", 2),
                ),
            )

            assertEquals(1, eventDao.getByGameId("g1").size)
            assertEquals(2, eventDao.getByGameId("g2").size)
        }
    }

    @Test
    fun `deleteByGameId удаляет события партии`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            eventDao.insertAll(listOf(event("g1:1", "g1", 1)))

            eventDao.deleteByGameId("g1")

            assertTrue(eventDao.getByGameId("g1").isEmpty())
        }
    }

    @Test
    fun `удаление партии каскадно удаляет события`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            eventDao.insertAll(listOf(event("g1:1", "g1", 1), event("g1:2", "g1", 2)))

            gameDao.deleteById("g1")

            assertTrue(eventDao.getByGameId("g1").isEmpty())
        }
    }
}
