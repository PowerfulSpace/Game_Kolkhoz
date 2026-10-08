package ru.kolkhoz.domain

import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.domain.model.ShotResult
import ru.kolkhoz.domain.rules.GameRules

/**
 * Общие фикстуры тестов правил игры.
 *
 * Тестовые помощники, а не часть domain API: в production-коде модуля их нет.
 */

/** Игроки `p0..pN` с заданными именами; позиция совпадает с индексом в порядке хода. */
internal fun playersOf(vararg names: String): List<Player> =
    names.mapIndexed { index, name ->
        Player(id = pid(index), name = name, position = index)
    }

/** Id игрока по позиции в порядке хода (`0..N-1`). */
internal fun pid(index: Int): PlayerId = PlayerId("p$index")

/** Новая активная партия из имён; падает, если валидация `createGame` не прошла. */
internal fun gameOf(vararg names: String): Game = gameOf(playersOf(*names))

/** Новая активная партия из списка игроков. */
internal fun gameOf(players: List<Player>): Game =
    GameRules.createGame(id = GameId("game-1"), createdAtMillis = 0L, players = players)
        .getOrElse { error("createGame неожиданно провалился: ${it.message}") }

/** Текущий игрок по пересчитанному состоянию партии. */
internal fun Game.currentPlayer(): PlayerId = GameRules.recompute(this).currentPlayerId

/** Счёт по пересчитанному состоянию партии. */
internal fun Game.scores(): Map<PlayerId, Int> = GameRules.recompute(this).scores

/** Удар текущего игрока (очередь проверяет сам `applyShot`). */
internal fun Game.shoot(result: ShotResult): Game =
    GameRules.applyShot(
        game = this,
        playerId = currentPlayer(),
        result = result,
        timestampMillis = 1_000L,
    )

/** Удар текущего игрока с результатом POCKET. */
internal fun Game.pocket(): Game = shoot(ShotResult.POCKET)

/** Удар текущего игрока с результатом MISS. */
internal fun Game.miss(): Game = shoot(ShotResult.MISS)
