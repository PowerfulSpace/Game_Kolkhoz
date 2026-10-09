package ru.kolkhoz.ui.mapper

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameState
import ru.kolkhoz.domain.model.ShotResult
import ru.kolkhoz.domain.rules.GameRules
import ru.kolkhoz.ui.model.GameHistoryItemUi
import ru.kolkhoz.ui.model.GameUiState
import ru.kolkhoz.ui.model.HistoryUiState
import ru.kolkhoz.ui.model.PlayerResultUi
import ru.kolkhoz.ui.model.PlayerUi
import ru.kolkhoz.ui.model.ResultUiState
import ru.kolkhoz.ui.model.ShotUi

/**
 * Маппинг domain-состояния в UI-state игрового экрана.
 *
 * Счёт берётся из [GameState.scores] (пересчитан [GameRules.recompute]),
 * `isCurrent` — по совпадению с [GameState.currentPlayerId].
 *
 * @param lastUndoneEvent Последнее отменённое событие — для redo.
 * @param canUndo Можно ли отменить последний удар.
 * @param isSaving Идёт ли сохранение партии (блокировка кнопок).
 */
fun GameState.toGameUiState(
    lastUndoneEvent: GameEvent?,
    canUndo: Boolean,
    isSaving: Boolean = false,
): GameUiState = GameUiState(
    players = players.map { player ->
        PlayerUi(
            id = player.id,
            name = player.name,
            score = scores[player.id] ?: 0,
            isCurrent = player.id == currentPlayerId,
        )
    },
    currentPlayerId = currentPlayerId,
    currentStreak = currentStreak,
    canUndo = canUndo,
    status = status,
    lastUndoneEvent = lastUndoneEvent,
    isSaving = isSaving,
)

/**
 * Маппинг партии в UI-state истории ударов.
 *
 * События идут свежими вперёд; имя игрока разрешается по
 * `playerId`, время форматируется в «HH:mm».
 */
fun Game.toHistoryUiState(): HistoryUiState {
    val shots = events.asReversed().map { event ->
        ShotUi(
            sequenceNumber = event.sequenceNumber,
            playerName = players.firstOrNull { it.id == event.playerId }?.name.orEmpty(),
            isPocket = event.result == ShotResult.POCKET,
            time = formatTime(event.timestampMillis),
        )
    }
    return HistoryUiState(
        events = shots,
        isEmpty = shots.isEmpty(),
    )
}

/**
 * Маппинг партии в UI-state результатов.
 *
 * Счёт пересчитывается через [GameRules.recompute]. Сортировка
 * по счёту убывание; [sortedByDescending] стабильна, поэтому при
 * равных счетах сохраняется порядок списка игроков (= position).
 */
fun Game.toResultUiState(): ResultUiState {
    val state = GameRules.recompute(this)
    val sorted = state.players.sortedByDescending { state.scores[it.id] ?: 0 }
    val players = sorted.mapIndexed { index, player ->
        PlayerResultUi(
            place = index + 1,
            name = player.name,
            score = state.scores[player.id] ?: 0,
        )
    }
    return ResultUiState(players = players)
}

/**
 * Маппинг партии в элемент истории игр.
 *
 * Дата — `createdAtMillis` в формате «dd.MM.yyyy» системным
 * поясом. Игроки пересчитываются через [GameRules.recompute] и
 * сортируются по счёту убывание — как в [toResultUiState].
 */
fun Game.toGameHistoryItemUi(): GameHistoryItemUi {
    val state = GameRules.recompute(this)
    val sorted = state.players.sortedByDescending { state.scores[it.id] ?: 0 }
    val players = sorted.mapIndexed { index, player ->
        PlayerResultUi(
            place = index + 1,
            name = player.name,
            score = state.scores[player.id] ?: 0,
        )
    }
    return GameHistoryItemUi(
        gameId = id.value,
        date = formatDate(createdAtMillis),
        playersCount = players.size,
        shotsCount = events.size,
        players = players,
    )
}

/** Форматирует `timestampMillis` в «dd.MM.yyyy» системным поясом. */
private fun formatDate(timestampMillis: Long): String {
    val date = Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}

/** Форматирует `timestampMillis` в «HH:mm» системным поясом. */
private fun formatTime(timestampMillis: Long): String {
    val time = Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return "%02d:%02d".format(time.hour, time.minute)
}
