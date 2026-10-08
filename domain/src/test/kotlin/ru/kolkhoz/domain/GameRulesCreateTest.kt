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

    @Test
    fun `position уникальны и в диапазоне от 0 до N-1 - success`() {
        val players = listOf(
            Player(pid(0), "Саша", 0),
            Player(pid(1), "Петя", 1),
            Player(pid(2), "Коля", 2),
        )

        val game = GameRules.createGame(GameId("g1"), 0L, players).getOrThrow()

        assertEquals(listOf(0, 1, 2), game.players.map { it.position })
    }

    @Test
    fun `position дублируются - failure InvalidPositions`() {
        val players = listOf(
            Player(pid(0), "Саша", 0),
            Player(pid(1), "Петя", 1),
            Player(pid(2), "Коля", 1),
        )

        val result = GameRules.createGame(GameId("g1"), 0L, players)

        assertTrue(result.isFailure)
        assertEquals(GameError.InvalidPositions(listOf(0, 1, 1)), result.exceptionOrNull())
    }

    @Test
    fun `position вне диапазона при 3 игроках - failure InvalidPositions`() {
        val players = listOf(
            Player(pid(0), "Саша", 0),
            Player(pid(1), "Петя", 1),
            Player(pid(2), "Коля", 5),
        )

        val result = GameRules.createGame(GameId("g1"), 0L, players)

        assertTrue(result.isFailure)
        assertEquals(GameError.InvalidPositions(listOf(0, 1, 5)), result.exceptionOrNull())
    }

    @Test
    fun `position не отсортированы но валидны - success и порядок списка сохраняется`() {
        val players = listOf(
            Player(pid(0), "Саша", 2),
            Player(pid(1), "Петя", 0),
            Player(pid(2), "Коля", 1),
        )

        val game = GameRules.createGame(GameId("g1"), 0L, players).getOrThrow()

        // createGame НЕ сортирует players: порядок списка — как передан, он же задаёт цикл.
        assertEquals(listOf(2, 0, 1), game.players.map { it.position })
        assertEquals(listOf("Саша", "Петя", "Коля"), game.players.map { it.name })
        assertEquals(pid(0), game.players.first().id)
    }

    @Test
    fun `имена в разном регистре - failure DuplicateName`() {
        val result = GameRules.createGame(GameId("g1"), 0L, playersOf("Саша", "саша", "Коля"))

        assertTrue(result.isFailure)
        assertEquals(GameError.DuplicateName("саша"), result.exceptionOrNull())
    }

    @Test
    fun `имена с ведущими пробелами - failure DuplicateName`() {
        val result = GameRules.createGame(GameId("g1"), 0L, playersOf(" Саша", "Саша", "Коля"))

        assertTrue(result.isFailure)
        assertEquals(GameError.DuplicateName("Саша"), result.exceptionOrNull())
    }

    @Test
    fun `имена с замыкающим пробелом - failure DuplicateName`() {
        val result = GameRules.createGame(GameId("g1"), 0L, playersOf("Саша", "Саша ", "Коля"))

        assertTrue(result.isFailure)
        assertEquals(GameError.DuplicateName("Саша "), result.exceptionOrNull())
    }

    @Test
    fun `разные имена в разном регистре - success а оригиналы сохраняются`() {
        val game = GameRules
            .createGame(GameId("g1"), 0L, playersOf("САША", "петя", "коля"))
            .getOrThrow()

        // Нормализация только для сравнения: данные пользователя не мутируются.
        assertEquals(listOf("САША", "петя", "коля"), game.players.map { it.name })
    }

    @Test
    fun `имя ровно 32 символа - success`() {
        val name32 = "А".repeat(32)

        val game = GameRules.createGame(GameId("g1"), 0L, playersOf(name32, "Петя", "Коля"))
            .getOrThrow()

        assertEquals(3, game.players.size)
        assertEquals(name32, game.players[0].name)
    }

    @Test
    fun `имя 33 символа - failure NameTooLong`() {
        val name33 = "А".repeat(33)

        val result = GameRules.createGame(GameId("g1"), 0L, playersOf(name33, "Петя", "Коля"))

        assertTrue(result.isFailure)
        assertEquals(GameError.NameTooLong(name33, 32), result.exceptionOrNull())
    }

    @Test
    fun `имя 100 символов - failure NameTooLong`() {
        val name100 = "А".repeat(100)

        val result = GameRules.createGame(GameId("g1"), 0L, playersOf(name100, "Петя", "Коля"))

        assertTrue(result.isFailure)
        assertEquals(GameError.NameTooLong(name100, 32), result.exceptionOrNull())
    }
}
