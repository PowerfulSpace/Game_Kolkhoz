package ru.kolkhoz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kolkhoz.domain.model.Game
import ru.kolkhoz.domain.model.GameEvent
import ru.kolkhoz.domain.model.ShotResult
import ru.kolkhoz.domain.repository.GameRepository
import ru.kolkhoz.domain.rules.GameRules
import ru.kolkhoz.ui.mapper.toGameUiState
import ru.kolkhoz.ui.model.GameUiState

private const val SAVE_ERROR_MESSAGE = "Не удалось сохранить партию"

/**
 * ViewModel игрового экрана (UI_SPEC.md, раздел 4).
 *
 * Наблюдает за активной партией ([GameRepository.observeActiveGame]),
 * пересчитывает состояние через [GameRules.recompute] и маппит в
 * [GameUiState]. Удары, undo, redo и завершение применяются через
 * [GameRules] и сохраняются обратно в репозиторий — все изменения
 * приходят обратно через Flow.
 *
 * Счёт в UI не считается — только отображается (ARCHITECTURE.md §7.2).
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState?>(null)

    /** Состояние игрового экрана; `null` — активной партии нет. */
    val uiState: StateFlow<GameUiState?> = _uiState.asStateFlow()

    /** Последняя загруженная партия (для синхронных вызовов GameRules). */
    private var currentGame: Game? = null

    /** Последнее отменённое событие — для redo (null — redo недоступен). */
    private var lastUndoneEvent: GameEvent? = null

    init {
        viewModelScope.launch {
            repository.observeActiveGame().collect { game ->
                currentGame = game
                _uiState.value = if (game != null) {
                    GameRules.recompute(game).toGameUiState(
                        lastUndoneEvent = lastUndoneEvent,
                        canUndo = game.events.isNotEmpty(),
                    )
                } else {
                    null
                }
            }
        }
    }

    /** Забил шар: +1 забившему, −1 штрафнику серии (правила — в domain). */
    fun onPocket() = applyShot(ShotResult.POCKET)

    /** Промах: серия сбрасывается, ход переходит следующему. */
    fun onMiss() = applyShot(ShotResult.MISS)

    /** Отмена последнего удара; событие запоминается для redo. */
    fun onUndo() {
        val game = currentGame ?: return
        val lastEvent = game.events.lastOrNull() ?: return
        lastUndoneEvent = lastEvent
        save(GameRules.undoLastShot(game))
    }

    /** Повтор отменённого удара (redo). */
    fun onRedo() {
        val game = currentGame ?: return
        val event = lastUndoneEvent ?: return
        lastUndoneEvent = null
        save(
            GameRules.applyShot(
                game = game,
                playerId = event.playerId,
                result = event.result,
                timestampMillis = System.currentTimeMillis(),
            ),
        )
    }

    /** Завершает партию (статус FINISHED, идемпотентно). */
    fun onFinishGame() {
        val game = currentGame ?: return
        save(GameRules.finishGame(game))
    }

    /** Сбрасывает сообщение об ошибке. */
    fun clearError() {
        _uiState.update { it?.copy(errorMessage = null) }
    }

    private fun applyShot(result: ShotResult) {
        val game = currentGame ?: return
        val state = GameRules.recompute(game)
        lastUndoneEvent = null
        save(
            GameRules.applyShot(
                game = game,
                playerId = state.currentPlayerId,
                result = result,
                timestampMillis = System.currentTimeMillis(),
            ),
        )
    }

    /**
     * Сохраняет партию; при ошибке — человеческое сообщение
     * в errorMessage (UI_SPEC.md, 9.2).
     */
    private fun save(game: Game) {
        viewModelScope.launch {
            try {
                repository.saveGame(game)
            } catch (e: Exception) {
                _uiState.update { it?.copy(errorMessage = SAVE_ERROR_MESSAGE) }
            }
        }
    }
}
