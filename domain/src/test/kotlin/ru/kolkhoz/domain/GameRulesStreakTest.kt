package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты правила серии: все штрафы серии идут тому, кто бил ДО её начала,
 * серия завершается промахом того же игрока.
 */
class GameRulesStreakTest {

    @Test
    fun `два забитых подряд — +2 забившему и -2 тому кто бил до серии`() {
        // Саша промахнулся → серия Пети начинается «с нуля».
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .miss()
            .pocket()
            .pocket()

        val scores = game.scores()
        assertEquals(2, scores[pid(1)])   // Петя забил дважды
        assertEquals(-2, scores[pid(0)])  // Саша — тот, кто бил до серии
        assertEquals(0, scores[pid(2)])
        assertEquals(0, scores[pid(3)])
        assertEquals(2, GameRules.recompute(game).currentStreak)
    }

    @Test
    fun `три забитых подряд — +3 и -3`() {
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .miss()
            .pocket()
            .pocket()
            .pocket()

        val scores = game.scores()
        assertEquals(3, scores[pid(1)])
        assertEquals(-3, scores[pid(0)])
        assertEquals(3, GameRules.recompute(game).currentStreak)
        assertEquals(pid(0), GameRules.recompute(game).streakPenaltyTargetId)
    }

    @Test
    fun `пять забитых подряд — +5 и -5`() {
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .miss()
            .pocket()
            .pocket()
            .pocket()
            .pocket()
            .pocket()

        val scores = game.scores()
        assertEquals(5, scores[pid(1)])
        assertEquals(-5, scores[pid(0)])
        assertEquals(5, GameRules.recompute(game).currentStreak)
        assertEquals(6, GameRules.recompute(game).eventsCount)
    }

    @Test
    fun `серия завершается промахом — счёт серии сохраняется но серия обнуляется`() {
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .miss()
            .pocket()
            .pocket()
            .miss()

        val state = GameRules.recompute(game)
        assertEquals(0, state.currentStreak)
        assertNull(state.streakPenaltyTargetId)
        assertEquals(pid(1), state.lastShooterId)      // промахнулся Петя
        assertEquals(pid(2), state.currentPlayerId)    // ход перешёл к Коле
        assertEquals(2, state.scores[pid(1)])          // результат серии не аннулирован
        assertEquals(-2, state.scores[pid(0)])
    }

    @Test
    fun `после серии следующий забивший штрафует того кто промахнулся`() {
        // Саша промах, Петя ×2, Петя промах, Коля забил → штраф Пете, а не Саше.
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .miss()
            .pocket()
            .pocket()
            .miss()
            .pocket()

        val scores = game.scores()
        assertEquals(1, scores[pid(2)])   // Коля +1
        assertEquals(1, scores[pid(1)])   // Петя: +2 за серию и −1 чужого штрафа
        assertEquals(-2, scores[pid(0)])  // Саша: только штрафы за серию
        assertEquals(0, scores[pid(3)])
        assertEquals(pid(2), GameRules.recompute(game).currentPlayerId)
    }

    @Test
    fun `пример из спецификации — Петя +2 Коля +1 Саша -3`() {
        // Саша MISS, Петя POCKET ×3, Петя MISS, Коля POCKET
        val game = gameOf("Саша", "Петя", "Коля", "Дима")
            .miss()
            .pocket()
            .pocket()
            .pocket()
            .miss()
            .pocket()

        val state = GameRules.recompute(game)
        assertEquals(6, state.eventsCount)
        assertEquals(2, state.scores[pid(1)])  // Петя: +3 за серию, −1 за удар Коли
        assertEquals(1, state.scores[pid(2)])  // Коля: +1
        assertEquals(-3, state.scores[pid(0)]) // Саша: −3 (вся серия Пети)
        assertEquals(0, state.scores[pid(3)])  // Дима не участвовал
        assertEquals(pid(2), state.currentPlayerId)
    }
}
