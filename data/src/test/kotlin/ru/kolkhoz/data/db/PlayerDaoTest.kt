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
import ru.kolkhoz.data.db.dao.GameDao
import ru.kolkhoz.data.db.dao.PlayerDao
import ru.kolkhoz.data.db.entity.GameEntity
import ru.kolkhoz.data.db.entity.PlayerEntity

/**
 * Тесты [PlayerDao] на Room in-memory (Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
class PlayerDaoTest {

    private lateinit var db: KolkhozDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var gameDao: GameDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KolkhozDatabase::class.java,
        ).build()
        playerDao = db.playerDao()
        gameDao = db.gameDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun player(
        id: String,
        gameId: String,
        name: String,
        position: Int,
        listOrder: Int,
    ) = PlayerEntity(id, gameId, name, position, listOrder)

    @Test
    fun `getByGameId сортирует по listOrder а не по position`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            // Намеренно в обратном порядке и с position, не совпадающим со списком.
            playerDao.insertAll(
                listOf(
                    player("p2", "g1", "Коля", position = 1, listOrder = 2),
                    player("p0", "g1", "Саша", position = 2, listOrder = 0),
                    player("p1", "g1", "Петя", position = 0, listOrder = 1),
                ),
            )

            val loaded = playerDao.getByGameId("g1")

            assertEquals(listOf("Саша", "Петя", "Коля"), loaded.map { it.name })
            assertEquals(listOf(0, 1, 2), loaded.map { it.listOrder })
            assertEquals(listOf(2, 0, 1), loaded.map { it.position })
        }
    }

    @Test
    fun `getByGameId возвращает только игроков своей партии`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            gameDao.upsert(GameEntity("g2", 2_000L, "ACTIVE"))
            playerDao.insertAll(
                listOf(
                    player("p0", "g1", "Саша", 0, 0),
                    player("p0", "g2", "Дима", 0, 0),
                    player("p1", "g2", "Коля", 1, 1),
                ),
            )

            val first = playerDao.getByGameId("g1")
            val second = playerDao.getByGameId("g2")

            assertEquals(1, first.size)
            assertEquals("Саша", first[0].name)
            assertEquals(2, second.size)
        }
    }

    @Test
    fun `deleteByGameId удаляет только игроков партии`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            gameDao.upsert(GameEntity("g2", 2_000L, "ACTIVE"))
            playerDao.insertAll(
                listOf(
                    player("p0", "g1", "Саша", 0, 0),
                    player("p0", "g2", "Дима", 0, 0),
                ),
            )

            playerDao.deleteByGameId("g1")

            assertTrue(playerDao.getByGameId("g1").isEmpty())
            assertEquals(1, playerDao.getByGameId("g2").size)
        }
    }

    @Test
    fun `удаление партии каскадно удаляет игроков`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            playerDao.insertAll(listOf(player("p0", "g1", "Саша", 0, 0)))

            gameDao.deleteById("g1")

            assertTrue(playerDao.getByGameId("g1").isEmpty())
        }
    }

    @Test
    fun `position и listOrder хранятся независимо`() {
        runTest {
            gameDao.upsert(GameEntity("g1", 1_000L, "ACTIVE"))
            playerDao.insertAll(listOf(player("p0", "g1", "Саша", position = 5, listOrder = 0)))

            val loaded = playerDao.getByGameId("g1").single()

            assertEquals(5, loaded.position)
            assertEquals(0, loaded.listOrder)
        }
    }
}
