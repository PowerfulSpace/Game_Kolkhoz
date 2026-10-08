package ru.kolkhoz.ui.model

import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.GameStatus
import ru.kolkhoz.domain.model.PlayerId

/**
 * Состояние игрового экрана (UI_SPEC.md, раздел 4).
 *
 * Полное UI-состояние партии: счёт и текущий игрок — производные
 * от истории событий (пересчёт в domain, маппинг — в
 * [ru.kolkhoz.ui.mapper.toGameUiState]).
 *
 * [lastUndoneEvent] — domain-модель в UI осознанно: для redo
 * после undo нужны `playerId` и `result`, а [GameEvent]
 * иммутабельный и без Android-зависимостей
 * (ARCHITECTURE.md §7.3).
 *
 * @property players Все игроки с текущим счётом.
 * @property currentPlayerId id текущего игрока; `null` — партии нет.
 * @property currentStreak Текущая серия ударов подряд.
 * @property canUndo Можно ли отменить последний удар.
 * @property status Статус партии.
 * @property lastUndoneEvent Последнее отменённое событие —
 *   для повтора (redo); `null` — redo недоступен.
 * @property isSaving Идёт ли сохранение партии; пока `true` —
 *   кнопки удара заблокированы (защита от дабл-тапа).
 * @property errorMessage Человеческое сообщение об ошибке;
 *   `null` — ошибки нет.
 */
data class GameUiState(
    val players: List<PlayerUi>,
    val currentPlayerId: PlayerId?,
    val currentStreak: Int,
    val canUndo: Boolean,
    val status: GameStatus,
    val lastUndoneEvent: GameEvent?,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
