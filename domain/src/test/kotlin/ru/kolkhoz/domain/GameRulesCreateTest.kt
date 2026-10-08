package ru.kolkhoz.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.rules.GameError
import ru.kolkhoz.domain.rules.GameRules

/**
 * Тесты `GameRules.createGame`: валидация состава и имён партии.
 */
class GameRulesCreateTest {

    @Test
    fun `создание партии на 3 игроков`() {
        val game = GameRules
            .createGame(GameId("g1"), createdAtMillis = 0L, playersOf("Саша", "Петя", "Коля"))
            .getOrThrow()

        assertEquals(3, game.players.size)
        assertEquals(listOf("Саша", "Петя", "Коля"), game.players.map { it.name })
        assertEquals(listOf(0, 1, 2), game.players.map { it.position })
        assertEquals(GameStatus.ACTIVE, game.status)
        assertTrue(game.events.isEmpty())
        assertEquals(0L, game.createdAtMillis)
    }

    @Test
    fun `создание партии на 4 игроков`() {
        val game = GameRules
            .createGame(GameId("g1"), 0L, playersOf("Саша", "Петя", "Коля", "Дима"))
            .getOrThrow()

        assertEquals(4, game.players.size)
        // Порядок игроков фиксируется и не меняется.
        assertEquals(listOf("Саша", "Петя", "Коля", "Дима"), game.players.map { it.name })
    }

    @Test
    fun `создание партии на 8 игроков`() {
        val names = listOf("И1", "И2", "И3", "И4", "И5", "И6", "И7", "И8")
        val game = GameRules
            .createGame(GameId("g1"), 0L, playersOf(*names.toTypedArray()))
            .getOrThrow()

        assertEquals(8, game.players.size)
        assertEquals(names, game.players.map { it.name })
    }

    @Test
    fun `ошибка - меньше 3 игроков`() {
        val invalidRosters: List<List<Player>> = listOf(
            emptyList(),
            playersOf("Саша"),
            playersOf("Саша", "Петя"),
        )

        for (roster in invalidRosters) {
            val result = GameRules.createGame(GameId("g1"), 0L, roster)

            assertTrue(result.isFailure, "ожидали ошибку для ${roster.size} игроков")
            assertEquals(GameError.TooFewPlayers(roster.size), result.exceptionOrNull())
        }
    }

    @Test
    fun `ошибка - больше 8 игроков`() {
        val result = GameRules.createGame(
            GameId("g1"),
            0L,
            playersOf("И1", "И2", "И3", "И4", "И5", "И6", "И7", "И8", "И9"),
        )

        assertTrue(result.isFailure)
        assertEquals(GameError.TooManyPlayers(9), result.exceptionOrNull())
    }

    @Test
    fun `ошибка - пустое имя`() {
        val result = GameRules.createGame(GameId("g1"), 0L, playersOf("Саша", "", "Коля"))

        assertTrue(result.isFailure)
        assertEquals(GameError.EmptyName(pid(1)), result.exceptionOrNull())
    }

    @Test
    fun `ошибка - имя из пробелов считается пустым`() {
        val result = GameRules.createGame(GameId("g1"), 0L, playersOf("Саша", "   ", "Коля"))

        assertTrue(result.isFailure)
        assertEquals(GameError.EmptyName(pid(1)), result.exceptionOrNull())
    }

    @Test
    fun `ошибка - дубликат имени`() {
        val result = GameRules.createGame(GameId("g1"), 0L, playersOf("Саша", "Петя", "Саша"))

        assertTrue(result.isFailure)
        assertEquals(GameError.DuplicateName("Саша"), result.exceptionOrNull())
    }
}
