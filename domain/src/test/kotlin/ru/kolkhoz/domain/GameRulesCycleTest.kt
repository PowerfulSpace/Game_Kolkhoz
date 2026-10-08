package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты циклического порядка игроков: после последнего идёт первый.
 */
class GameRulesCycleTest {

    @Test
    fun `при 4 игроках цикл 0 - 1 - 2 - 3 - 0`() {
        val game = gameOf("И0", "И1", "И2", "И3")

        assertNextCycle(game, from = 0, 1, 2, 3, 0)
        assertEquals(pid(3), GameRules.previousPlayerIdInCycle(game, pid(0)))
        assertEquals(pid(0), GameRules.previousPlayerIdInCycle(game, pid(1)))
    }

    @Test
    fun `при 3 игроках цикл 0 - 1 - 2 - 0`() {
        val game = gameOf("И0", "И1", "И2")

        assertNextCycle(game, from = 0, 1, 2, 0)
        assertEquals(pid(2), GameRules.previousPlayerIdInCycle(game, pid(0)))
    }

    @Test
    fun `при 8 игроках цикл 0 - 1 - 2 - 3 - 4 - 5 - 6 - 7 - 0`() {
        val game = gameOf("И0", "И1", "И2", "И3", "И4", "И5", "И6", "И7")

        assertNextCycle(game, from = 0, 1, 2, 3, 4, 5, 6, 7, 0)
        assertEquals(pid(7), GameRules.previousPlayerIdInCycle(game, pid(0)))
        assertEquals(pid(3), GameRules.previousPlayerIdInCycle(game, pid(4)))
    }

    /** Проверяет, что циклический переход от [from] даёт последовательность [expected]. */
    private fun assertNextCycle(game: Game, from: Int, vararg expected: Int) {
        var current = pid(from)
        for (next in expected) {
            assertEquals(pid(next), GameRules.nextPlayerId(game, current))
            current = pid(next)
        }
    }
}
