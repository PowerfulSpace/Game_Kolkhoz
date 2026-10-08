package ru.kolkhoz.data.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.model.PlayerId

/**
 * Тесты [PlayerMapper]: domain ↔ Room-строка.
 */
class PlayerMapperTest {

    private val gameId = GameId("game-1")

    @Test
    fun `round trip сохраняет игрока включая position`() {
        val player = Player(PlayerId("p1"), "Саша", position = 2)

        val restored = player.toEntity(gameId, listOrder = 0).toDomain()

        assertEquals(player, restored)
    }

    @Test
    fun `toEntity записывает партию gameId id имя position и listOrder`() {
        val entity = Player(PlayerId("p1"), "Петя", position = 0)
            .toEntity(gameId, listOrder = 3)

        assertEquals("game-1", entity.gameId)
        assertEquals("p1", entity.id)
        assertEquals("Петя", entity.name)
        assertEquals(0, entity.position)
        assertEquals(3, entity.listOrder)
    }

    @Test
    fun `position и listOrder не перепутываются`() {
        // position = 2 (метаданные) при listOrder = 0 (порядок в списке):
        // оба значения обязаны пережить round trip независимо.
        val player = Player(PlayerId("p2"), "Коля", position = 2)
        val entity = player.toEntity(gameId, listOrder = 0)

        assertEquals(2, entity.position)
        assertEquals(0, entity.listOrder)
        assertEquals(2, entity.toDomain().position)
    }
}
