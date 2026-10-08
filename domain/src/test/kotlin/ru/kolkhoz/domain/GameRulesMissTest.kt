package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты правила MISS: счёт не меняется, ход переходит, lastShooterId обновляется.
 */
class GameRulesMissTest {

    @Test
    fun `промах — счёт не меняется`() {
        val game = gameOf("Саша", "Петя", "Коля").miss()

        assertEquals(
            mapOf(pid(0) to 0, pid(1) to 0, pid(2) to 0),
            game.scores(),
        )
        assertEquals(0, GameRules.recompute(game).currentStreak)
    }

    @Test
    fun `промах — ход переходит следующему игроку`() {
        val game = gameOf("Саша", "Петя", "Коля").miss()

        assertEquals(pid(1), game.currentPlayer())
    }

    @Test
    fun `промах — lastShooterId обновляется на промахнувшегося`() {
        val before = GameRules.recompute(gameOf("Саша", "Петя", "Коля"))
        assertNull(before.lastShooterId)

        val after = GameRules.recompute(gameOf("Саша", "Петя", "Коля").miss())
        assertEquals(pid(0), after.lastShooterId)
        assertNull(after.streakPenaltyTargetId)
    }

    @Test
    fun `несколько промахов подряд — ход идёт по циклу`() {
        var game = gameOf("Саша", "Петя", "Коля", "Дима")
        val expectedOrder = listOf(pid(1), pid(2), pid(3), pid(0))

        for (expected in expectedOrder) {
            game = game.miss()
            assertEquals(expected, game.currentPlayer())
        }

        assertEquals(4, GameRules.recompute(game).eventsCount)
        assertEquals(pid(3), GameRules.recompute(game).lastShooterId)
        assertTrue(game.scores().values.all { it == 0 })
    }
}
