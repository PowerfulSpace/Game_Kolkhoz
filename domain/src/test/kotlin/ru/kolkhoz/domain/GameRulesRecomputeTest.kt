package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты `GameRules.recompute`: производное состояние, идемпотентность.
 */
class GameRulesRecomputeTest {

    @Test
    fun `recompute из пустой игры — начальное состояние`() {
        val game = gameOf("Саша", "Петя", "Коля")
        val state = GameRules.recompute(game)

        assertEquals(game.players, state.players)
        assertEquals(
            mapOf(pid(0) to 0, pid(1) to 0, pid(2) to 0),
            state.scores,
        )
        assertEquals(pid(0), state.currentPlayerId)  // очередь начинается с первого игрока
        assertEquals(0, state.currentStreak)
        assertNull(state.lastShooterId)               // событий ещё нет
        assertNull(state.streakPenaltyTargetId)
        assertEquals(GameStatus.ACTIVE, state.status)
        assertEquals(0, state.eventsCount)
    }

    @Test
    fun `recompute после N событий — счёт совпадает с ожидаемым`() {
        // Саша забил, Саша промахнулся, Петя забил дважды.
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .pocket()
            .miss()
            .pocket()
            .pocket()

        val state = GameRules.recompute(game)

        assertEquals(4, state.eventsCount)
        assertEquals(listOf(1, 2, 3, 4), game.events.map { it.sequenceNumber })
        assertEquals(
            mapOf(pid(0) to -1, pid(1) to 2, pid(2) to 0, pid(3) to -1),
            state.scores,
        )
        assertEquals(pid(1), state.currentPlayerId)
        assertEquals(2, state.currentStreak)
        assertEquals(pid(1), state.lastShooterId)
        assertEquals(pid(0), state.streakPenaltyTargetId)
    }

    @Test
    fun `recompute идемпотентен — дважды вызванный даёт тот же результат`() {
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .miss()
            .pocket()
            .pocket()
            .miss()
            .pocket()

        val first = GameRules.recompute(game)
        val second = GameRules.recompute(game)
        // Копия с новым списком событий даёт идентичный результат.
        val third = GameRules.recompute(game.copy(events = game.events.toList()))

        assertEquals(first, second)
        assertEquals(first, third)
    }
}
