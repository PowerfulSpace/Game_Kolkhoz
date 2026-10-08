package ru.kolkhoz.domain.rules

import ru.kolkhoz.domain.model.EventId
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.GameState
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.domain.model.ShotResult

/**
 * Ошибка валидации партии.
 *
 * Наследует [Exception] исключительно потому, что канал ошибок `Result` в Kotlin
 * физически хранит `Throwable` (`Result.failure(...)` принимает только его).
 * Это НЕ «исключение для управления логикой» из Part 5: поток управления не прерывается,
 * вызывающий код обязан прочитать ошибку через `Result.exceptionOrNull()` /
 * `getOrElse` и принять решение сам.
 *
 * Выбран `sealed class`, а не enum, потому что варианты несут разные данные
 * (`actual`, `playerId`, `name`) — enum заставил бы плодить отдельные поля-заглушки.
 */
sealed class GameError(message: String) : Exception(message) {

    /** В партии меньше 3 игроков. */
    data class TooFewPlayers(val actual: Int) :
        GameError("Слишком мало игроков: $actual, минимум 3")

    /** В партии больше 8 игроков. */
    data class TooManyPlayers(val actual: Int) :
        GameError("Слишком много игроков: $actual, максимум 8")

    /** У игрока пустое (или состоящее только из пробелов) имя. */
    data class EmptyName(val playerId: PlayerId) :
        GameError("Игрок $playerId имеет пустое имя")

    /** Имя повторяется в партии. */
    data class DuplicateName(val name: String) :
        GameError("Имя повторяется в партии: \"$name\"")
}

/**
 * Игровые правила «Колхоза» — единственный источник игровой логики.
 *
 * Все функции чистые, детерминированные и синхронные:
 * - нет обращений к часам — время передаётся параметром (`timestampMillis`, `createdAtMillis`);
 * - нет `Random`, сети, БД, Android-типов, coroutines;
 * - нет мутации — только `data class.copy()` и новые списки;
 * - счёт нигде не хранится: он всегда пересчитывается из событий ([recompute]).
 *
 * Порядок игроков циклический и фиксируется при создании партии:
 * после последнего игрока списка идёт первый.
 */
object GameRules {

    /** Минимальное число игроков в партии. */
    private const val MIN_PLAYERS = 3

    /** Максимальное число игроков в партии. */
    private const val MAX_PLAYERS = 8

    /**
     * Создаёт новую партию с фиксированным порядком игроков.
     *
     * Валидация (в этом порядке):
     * 1. число игроков от [MIN_PLAYERS] до [MAX_PLAYERS];
     * 2. имена не пустые (и не состоят только из пробелов);
     * 3. имена уникальны (сравнение точное, без нормализации регистра).
     *
     * Возвращает `Result`, а не бросает исключения: Part 5 запрещает исключения
     * для управления логикой, а `Result` — штатный механизм Kotlin для «любого»
     * исхода без исключений. Это решение заменяет сигнатуру `createGame(...): Game`
     * из Part 3 (противоречие разрешено автором промпта).
     *
     *
     * @param id идентификатор партии
     * @param createdAtMillis момент создания, приходит извне
     * @param players игроки в фиксированном порядке хода
     * @return `Result.success(game)` либо `Result.failure` с [GameError]
     */
    fun createGame(
        id: GameId,
        createdAtMillis: Long,
        players: List<Player>,
    ): Result<Game> {
        if (players.size < MIN_PLAYERS) {
            return Result.failure(GameError.TooFewPlayers(players.size))
        }
        if (players.size > MAX_PLAYERS) {
            return Result.failure(GameError.TooManyPlayers(players.size))
        }
        players.firstOrNull { it.name.isBlank() }?.let {
            return Result.failure(GameError.EmptyName(it.id))
        }
        val seen = mutableSetOf<String>()
        for (player in players) {
            if (!seen.add(player.name)) {
                return Result.failure(GameError.DuplicateName(player.name))
            }
        }
        return Result.success(
            Game(
                id = id,
                createdAtMillis = createdAtMillis,
                players = players.toList(),
                events = emptyList(),
                status = GameStatus.ACTIVE,
            )
        )
    }

    /**
     * Пересчитывает производное [GameState] из событий партии.
     *
     * Чистая и идемпотентная функция: `recompute(recompute(g)…) == recompute(g)`,
     * события обрабатываются по возрастанию [GameEvent.sequenceNumber].
     *
     * Правила пересчёта:
     * - **POCKET**: забившему +1; «предыдущему игроку» −1; серия растёт на 1;
     *   ход НЕ переходит ([GameState.currentPlayerId] = забивший).
     * - **MISS**: счёт не меняется; серия обнуляется; ход переходит следующему по циклу.
     * - **Кому −1 при POCKET** (правило серии):
     *   * первое событие партии → цикловой предшественник забившего
     *     (у первого игрока — последний в списке);
     *   * продолжение серии (`shooter == lastShooterId`) → [GameState.streakPenaltyTargetId],
     *     т.е. тому, кто бил до начала серии;
     *   * начало новой серии → [GameState.lastShooterId].
     *
     * Никогда не падает на корректной [Game]: пустой список событий — валидное
     * начальное состояние. Единственный `require` — отсутствие игроков, что невозможно
     * через [createGame] и потому является нарушением инварианта, а не игровым сценарием.
     *
     * @param game партия, состояние которой пересчитывается
     * @return производное состояние партии
     */
    fun recompute(game: Game): GameState {
        val players = game.players
        require(players.isNotEmpty()) {
            "У партии ${game.id.value} нет игроков — инвариант нарушен: " +
                "Game создаётся только через createGame (3..8 игроков)"
        }

        val scores = players.associate { it.id to 0 }.toMutableMap()
        var currentPlayerId = players.first().id
        var currentStreak = 0
        var lastShooterId: PlayerId? = null
        var streakPenaltyTargetId: PlayerId? = null

        for (event in game.events.sortedBy { it.sequenceNumber }) {
            when (event.result) {
                ShotResult.POCKET -> {
                    val shooter = event.playerId
                    val penaltyTarget = resolveStreakPenaltyTarget(
                        players = players,
                        shooter = shooter,
                        lastShooterId = lastShooterId,
                        streakPenaltyTargetId = streakPenaltyTargetId,
                    )
                    scores.addPoints(shooter, 1)
                    if (penaltyTarget != null) {
                        scores.addPoints(penaltyTarget, -1)
                    }
                    currentStreak += 1
                    lastShooterId = shooter
                    streakPenaltyTargetId = penaltyTarget
                    // Забивший продолжает бить — ход не переходит.
                    currentPlayerId = shooter
                }

                ShotResult.MISS -> {
                    currentStreak = 0
                    streakPenaltyTargetId = null
                    lastShooterId = event.playerId
                    currentPlayerId = nextInCycle(players, event.playerId)
                }
            }
        }

        return GameState(
            players = players,
            scores = scores,
            currentPlayerId = currentPlayerId,
            currentStreak = currentStreak,
            lastShooterId = lastShooterId,
            streakPenaltyTargetId = streakPenaltyTargetId,
            status = game.status,
            eventsCount = game.events.size,
        )
    }

    /**
     * Применяет удар и возвращает **новую** [Game] с добавленным событием.
     * Исходная партия не мутируется.
     *
     * Поведение:
     * - если [Game.status] == [GameStatus.FINISHED] — возвращает [game] **без изменений**
     *   (удар в завершённую партию игнорируется; это защита от бага UI, а не бизнес-сценарий,
     *   поэтому никаких Result и исключений здесь нет);
     * - иначе проверяет очередь: удар принимается только от игрока, чей ход
     *   по пересчитанному состоянию.
     *
     * `sequenceNumber` и `EventId` вычисляются детерминированно из размера списка
     * событий: в domain запрещён `Random`, а время для идентификатора не используется.
     *
     * @param game партия, к которой применяется удар
     * @param playerId кто бьёт; должен совпадать с `recompute(game).currentPlayerId`
     * @param result исход удара
     * @param timestampMillis момент удара, приходит извне (domain не читает часы)
     * @return новая партия с добавленным событием либо [game] без изменений, если она завершена
     * @throws IllegalArgumentException если [playerId] не совпадает с текущим игроком.
     *   Это программная ошибка UI («не тот ход»), а не пользовательский сценарий:
     *   UI всегда знает `GameState.currentPlayerId`.
     */
    fun applyShot(
        game: Game,
        playerId: PlayerId,
        result: ShotResult,
        timestampMillis: Long,
    ): Game {
        // Проверка статуса идёт ДО require: на завершённой игре вызов игнорируется, не падает.
        if (game.status == GameStatus.FINISHED) return game

        val state = recompute(game)
        require(playerId == state.currentPlayerId) {
            "Ход $playerId не совпадает с текущим ${state.currentPlayerId} " +
                "в партии ${game.id.value} — программная ошибка UI"
        }

        val sequenceNumber = game.events.size + 1
        val event = GameEvent(
            id = EventId("${game.id.value}:$sequenceNumber"),
            sequenceNumber = sequenceNumber,
            playerId = playerId,
            result = result,
            timestampMillis = timestampMillis,
        )
        return game.copy(events = game.events + event)
    }

    /**
     * Отменяет последнее событие: возвращает **новую** [Game] без него.
     *
     * Undo НЕ является ручной правкой счёта: состояние до события восстанавливается
     * автоматически, потому что счёт — производное от оставшихся событий ([recompute]).
     *
     * Если событий нет — возвращает [game] без изменений.
     * Статус партии при отмене не меняется.
     *
     * @param game партия с событиями
     * @return партия без последнего события либо [game], если событий не было
     */
    fun undoLastShot(game: Game): Game {
        if (game.events.isEmpty()) return game
        return game.copy(events = game.events.dropLast(1))
    }

    /**
     * Завершает партию: возвращает **новую** [Game] со статусом [GameStatus.FINISHED].
     *
     * Идемпотентно: повторный вызов на завершённой партии возвращает её без изменений.
     * События и счёт не затрагиваются.
     *
     * @param game партия, которую нужно завершить
     * @return партия со статусом FINISHED
     */
    fun finishGame(game: Game): Game {
        if (game.status == GameStatus.FINISHED) return game
        return game.copy(status = GameStatus.FINISHED)
    }

    /**
     * Возвращает id игрока, следующего по циклу после [currentPlayerId].
     *
     * Цикл совпадает с порядком списка [Game.players]: после последнего идёт первый.
     *
     * @param game партия с игроками
     * @param currentPlayerId id, относительно которого ищется следующий игрок
     * @return id следующего по циклу игрока
     * @throws IllegalArgumentException если [currentPlayerId] не принадлежит партии —
     *   программная ошибка вызывающего кода, не игровой сценарий
     */
    fun nextPlayerId(game: Game, currentPlayerId: PlayerId): PlayerId {
        requirePlayerExists(game, currentPlayerId)
        return nextInCycle(game.players, currentPlayerId)
    }

    /**
     * Возвращает id игрока, идущего по циклу **перед** [currentPlayerId].
     *
     * Нужен, чтобы у первого игрока партии тоже был «предыдущий»:
     * для игрока с позицией 0 это последний игрок списка.
     *
     * @param game партия с игроками
     * @param currentPlayerId id, относительно которого ищется предыдущий игрок
     * @return id предыдущего по циклу игрока
     * @throws IllegalArgumentException если [currentPlayerId] не принадлежит партии —
     *   программная ошибка вызывающего кода, не игровой сценарий
     */
    fun previousPlayerIdInCycle(game: Game, currentPlayerId: PlayerId): PlayerId {
        requirePlayerExists(game, currentPlayerId)
        return previousInCycle(game.players, currentPlayerId)
    }

    /**
     * Определяет, кому идёт штраф −1 за данный [shooter] при POCKET.
     *
     * @param players игроки партии в порядке цикла
     * @param shooter кто забил
     * @param lastShooterId кто бил перед этим событием; `null` — событий ещё не было
     * @param streakPenaltyTargetId кому штрафовали в рамках текущей серии
     * @return id получателя штрафа; `null` теоретически возможен только если штрафовать некого
     */
    private fun resolveStreakPenaltyTarget(
        players: List<Player>,
        shooter: PlayerId,
        lastShooterId: PlayerId?,
        streakPenaltyTargetId: PlayerId?,
    ): PlayerId? = when {
        // Первое событие партии: «предыдущий» — цикловой predecessor забившего.
        lastShooterId == null -> previousInCycle(players, shooter)

        // Продолжение серии: штраф уже закреплён за тем, кто бил до её начала.
        // `?:` — защитный fallback для (теоретически) некорректных данных, чтобы recompute не падал.
        shooter == lastShooterId -> streakPenaltyTargetId ?: previousInCycle(players, shooter)

        // Начало новой серии: штрафует тот, кто бил непосредственно перед ней.
        else -> lastShooterId
    }

    /**
     * Следующий по циклу игрок. Детерминированно и не бросает исключений:
     * если id не найден, возвращается он же (защита recompute от некорректных данных).
     */
    private fun nextInCycle(players: List<Player>, current: PlayerId): PlayerId {
        val index = players.indexOfFirst { it.id == current }
        if (index < 0) return current
        return players[(index + 1) % players.size].id
    }

    /**
     * Предыдущий по циклу игрок. Детерминированно и не бросает исключений:
     * если id не найден, возвращается он же (защита recompute от некорректных данных).
     */
    private fun previousInCycle(players: List<Player>, current: PlayerId): PlayerId {
        val index = players.indexOfFirst { it.id == current }
        if (index < 0) return current
        return players[(index - 1 + players.size) % players.size].id
    }

    /** Начисляет [delta] очков игроку, если он есть в таблице (защита от некорректных данных). */
    private fun MutableMap<PlayerId, Int>.addPoints(playerId: PlayerId, delta: Int) {
        val current = this[playerId] ?: return
        this[playerId] = current + delta
    }

    /** Фиксирует инвариант: игрок должен принадлежать партии. */
    private fun requirePlayerExists(game: Game, playerId: PlayerId) {
        require(game.players.any { it.id == playerId }) {
            "Игрок $playerId не найден в партии ${game.id.value} — программная ошибка вызывающего кода"
        }
    }
}
