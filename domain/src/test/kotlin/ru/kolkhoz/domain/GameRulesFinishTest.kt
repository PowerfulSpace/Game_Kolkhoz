package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.ShotResult
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты `GameRules.finishGame` и поведения ударов в завершённой партии.
 */
class GameRulesFinishTest {

    @Test
    fun `завершение игры — status FINISHED`() {
        val game = gameOf("Саша", "Петя", "Коля").pocket()

        val finished = GameRules.finishGame(game)

        assertEquals(GameStatus.FINISHED, finished.status)
        assertEquals(GameStatus.FINISHED, GameRules.recompute(finished).status)
        // События и счёт при завершении не затрагиваются.
        assertEquals(game.events, finished.events)
        assertEquals(game.scores(), finished.scores())
        assertEquals(game.copy(status = GameStatus.FINISHED), finished)
    }

    @Test
    fun `finishGame идемпотентен — повторный вызов не меняет игру`() {
        val once = GameRules.finishGame(gameOf("Саша", "Петя", "Коля"))

        val twice = GameRules.finishGame(once)

        assertEquals(once, twice)
        assertEquals(GameStatus.FINISHED, twice.status)
    }

    @Test
    fun `applyShot на завершённой игре — возвращает game без изменений и не бросает`() {
        val finished = GameRules.finishGame(gameOf("Саша", "Петя", "Коля").pocket())
        val eventsBefore = finished.events.toList()

        // pid(2) НЕ текущий игрок: проверка статуса обязано идти раньше require,
        // поэтому вызов игнорируется, а не падает с IllegalArgumentException.
        val after = GameRules.applyShot(finished, pid(2), ShotResult.MISS, 999L)

        assertEquals(finished, after)
        assertEquals(eventsBefore, after.events)
        assertEquals(GameStatus.FINISHED, after.status)
        assertEquals(finished.scores(), after.scores())
    }
}
