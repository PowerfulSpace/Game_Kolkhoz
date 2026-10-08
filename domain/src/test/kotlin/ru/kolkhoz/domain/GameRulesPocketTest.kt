package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.model.EventId
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.ShotResult
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты правила POCKET: +1 забившему, −1 «предыдущему игроку», ход не переходит.
 */
class GameRulesPocketTest {

    @Test
    fun `один забитый шар — забивший +1 а предыдущий игрок -1`() {
        val game = gameOf("Саша", "Петя", "Коля", "Дима").pocket()

        val scores = game.scores()
        assertEquals(1, scores[pid(0)])   // Саша забил
        assertEquals(-1, scores[pid(3)])  // Дима — предыдущий по циклу
        assertEquals(0, scores[pid(1)])
        assertEquals(0, scores[pid(2)])
    }

    @Test
    fun `первый удар партии — штраф последнему игроку в списке`() {
        val game = gameOf("Саша", "Петя", "Коля", "Дима").pocket()

        // У первого игрока есть «предыдущий» — последний в списке.
        assertEquals(pid(3), GameRules.previousPlayerIdInCycle(game, pid(0)))
        assertEquals(-1, game.scores()[pid(3)])
        assertEquals(1, GameRules.recompute(game).eventsCount)
    }

    @Test
    fun `первый удар партии забивает не первый игрок — штраф предыдущему по циклу`() {
        // Собираем Game вручную: applyShot не позволит сделать первым удар игрока
        // не с первой позиции, но recompute обязан корректно обработать любой первый удар.
        val game = Game(
            id = GameId("game-1"),
            createdAtMillis = 0L,
            players = playersOf("Саша", "Петя", "Коля", "Дима"),
            events = listOf(
                GameEvent(
                    id = EventId("game-1:1"),
                    sequenceNumber = 1,
                    playerId = pid(1),
                    result = ShotResult.POCKET,
                    timestampMillis = 1_000L,
                ),
            ),
            status = GameStatus.ACTIVE,
        )

        val scores = GameRules.recompute(game).scores
        assertEquals(1, scores[pid(1)])   // Петя забил первым
        assertEquals(-1, scores[pid(0)])  // Саша — предыдущий по циклу от Пети
        assertEquals(0, scores[pid(2)])
        assertEquals(0, scores[pid(3)])
    }

    @Test
    fun `забивший продолжает бить — ход не переходит`() {
        val game = gameOf("Саша", "Петя", "Коля").pocket()

        assertEquals(pid(0), game.currentPlayer())
        assertEquals(1, GameRules.recompute(game).currentStreak)
        assertEquals(pid(0), GameRules.recompute(game).lastShooterId)
    }

    @Test
    fun `отрицательный счёт разрешён — игрок уходит в минус`() {
        val game = gameOf("Саша", "Петя", "Коля", "Дима").pocket().pocket()

        val scores = game.scores()
        assertEquals(2, scores[pid(0)])   // Саша забил дважды
        assertEquals(-2, scores[pid(3)])  // Дима получил оба штрафа
        assertTrue(scores.getValue(pid(3)) < 0)
    }

    @Test
    fun `ошибка — удар не в свой ход бросает IllegalArgumentException`() {
        // Обязательный тест по решению автора промпта: нарушение очереди —
        // программная ошибка UI, а не бизнес-сценарий.
        val game = gameOf("Саша", "Петя", "Коля")

        assertThrows(IllegalArgumentException::class.java) {
            GameRules.applyShot(game, pid(1), ShotResult.POCKET, 1_000L)
        }

        // Исходная партия не изменилась.
        assertTrue(game.events.isEmpty())
        assertEquals(GameStatus.ACTIVE, game.status)
    }
}
