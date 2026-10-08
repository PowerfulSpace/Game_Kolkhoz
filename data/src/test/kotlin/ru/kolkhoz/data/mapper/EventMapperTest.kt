package ru.kolkhoz.data.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import ru.kolkhoz.data.db.entity.GameEventEntity
import ru.kolkhoz.domain.model.EventId
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.domain.model.ShotResult

/**
 * Тесты [EventMapper]: domain ↔ Room-строка, маппинг enum'ов.
 */
class EventMapperTest {

    private val gameId = GameId("game-1")

    private fun event(
        result: ShotResult = ShotResult.POCKET,
        sequenceNumber: Int = 1,
    ) = GameEvent(
        id = EventId("game-1:$sequenceNumber"),
        sequenceNumber = sequenceNumber,
        playerId = PlayerId("p1"),
        result = result,
        timestampMillis = 1_000L,
    )

    @Test
    fun `round trip сохраняет событие`() {
        val original = event(result = ShotResult.MISS, sequenceNumber = 7)

        val restored = original.toEntity(gameId).toDomain()

        assertEquals(original, restored)
    }

    @Test
    fun `enum маппится в строки и обратно`() {
        assertEquals("POCKET", event(ShotResult.POCKET).toEntity(gameId).result)
        assertEquals("MISS", event(ShotResult.MISS).toEntity(gameId).result)

        assertEquals(ShotResult.POCKET, GameEventEntity("game-1:1", "game-1", 1, "p1", "POCKET", 1).toDomain().result)
        assertEquals(ShotResult.MISS, GameEventEntity("game-1:1", "game-1", 1, "p1", "MISS", 1).toDomain().result)
    }

    @Test
    fun `неизвестный result - ошибка`() {
        val corrupted = GameEventEntity("game-1:1", "game-1", 1, "p1", "CUE_BALL", 1)

        assertThrows(IllegalArgumentException::class.java) {
            corrupted.toDomain()
        }
    }
}
