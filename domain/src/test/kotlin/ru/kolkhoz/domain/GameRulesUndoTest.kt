package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты `GameRules.undoLastShot`: отмена — это удаление последнего события + пересчёт,
 * а не ручная правка счёта.
 */
class GameRulesUndoTest {

    @Test
    fun `undo после одиночного забитого — состояние как до удара`() {
        val gameBefore = gameOf("Саша", "Петя", "Коля")
        val stateBefore = GameRules.recompute(gameBefore)

        val gameAfterShot = gameBefore.pocket()
        val undone = GameRules.undoLastShot(gameAfterShot)

        assertEquals(stateBefore, GameRules.recompute(undone))
        assertEquals(gameBefore, undone)
    }

    @Test
    fun `undo после серии из 3 — состояние как до серии`() {
        val gameBeforeSeries = gameOf("Саша", "Петя", "Коля", "Дима").miss()
        val stateBeforeSeries = GameRules.recompute(gameBeforeSeries)

        var gameWithStreak = gameBeforeSeries
        repeat(3) { gameWithStreak = gameWithStreak.pocket() }
        assertEquals(3, GameRules.recompute(gameWithStreak).currentStreak)

        // Три undo подряд снимают всю серию.
        var undone = gameWithStreak
        repeat(3) { undone = GameRules.undoLastShot(undone) }
        assertEquals(stateBeforeSeries, GameRules.recompute(undone))

        // Один undo снимает ровно последнее событие — состояние совпадает
        // с игрой, где серия длилась два удара.
        val oneUndo = GameRules.undoLastShot(gameWithStreak)
        assertEquals(gameBeforeSeries.pocket().pocket(), oneUndo)
        assertEquals(2, GameRules.recompute(oneUndo).currentStreak)
    }

    @Test
    fun `undo после промаха — состояние как до промаха`() {
        val gameBefore = gameOf("Саша", "Петя", "Коля").pocket()
        val stateBefore = GameRules.recompute(gameBefore)

        val gameAfterMiss = gameBefore.miss()
        val undone = GameRules.undoLastShot(gameAfterMiss)

        assertEquals(stateBefore, GameRules.recompute(undone))
        assertEquals(pid(0), GameRules.recompute(undone).currentPlayerId)
    }

    @Test
    fun `undo на пустой игре — ничего не меняется`() {
        val game = gameOf("Саша", "Петя", "Коля")

        val undone = GameRules.undoLastShot(game)

        assertSame(game, undone)
        assertTrue(undone.events.isEmpty())
        assertEquals(GameRules.recompute(game), GameRules.recompute(undone))
    }

    @Test
    fun `undo не мутирует исходную Game`() {
        val original = gameOf("Саша", "Петя", "Коля").pocket()
        val originalEvents = original.events.toList()
        val originalState = GameRules.recompute(original)

        val undone = GameRules.undoLastShot(original)

        assertEquals(originalEvents, original.events)
        assertEquals(1, original.events.size)
        assertEquals(originalState, GameRules.recompute(original))
        assertTrue(undone.events.isEmpty())
        assertTrue(original !== undone)
    }

    @Test
    fun `несколько undo подряд выигрывают всё до пустой игры`() {
        val empty = gameOf("Саша", "Петя", "Коля")
        val withEvents = empty.pocket().pocket().miss()
        assertEquals(3, GameRules.recompute(withEvents).eventsCount)

        var undone = withEvents
        repeat(3) { undone = GameRules.undoLastShot(undone) }
        assertEquals(empty, undone)
        assertEquals(GameRules.recompute(empty), GameRules.recompute(undone))

        // Четвёртый undo на пустой игре безопасен.
        val extraUndo = GameRules.undoLastShot(undone)
        assertEquals(empty, extraUndo)
    }

    @Test
    fun `undo + повторный удар — состояние корректное`() {
        val afterFirstMiss = gameOf("Саша", "Петя", "Коля").miss() // ход: Петя
        val afterPocket = afterFirstMiss.pocket()                   // Петя +1, Саша -1
        assertEquals(1, afterPocket.scores()[pid(1)])

        val undone = GameRules.undoLastShot(afterPocket)
        assertEquals(afterFirstMiss, undone)

        // Повторный удар делает тот же игрок (Петя) — на этот раз промах.
        val afterRetry = undone.miss()
        val state = GameRules.recompute(afterRetry)

        assertEquals(2, state.eventsCount)
        assertEquals(
            mapOf(pid(0) to 0, pid(1) to 0, pid(2) to 0),
            state.scores,
        )
        assertEquals(pid(2), state.currentPlayerId)
        assertEquals(pid(1), state.lastShooterId)
    }
}
