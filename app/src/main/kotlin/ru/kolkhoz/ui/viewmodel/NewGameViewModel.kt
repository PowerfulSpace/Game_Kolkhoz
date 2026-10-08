package ru.kolkhoz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.model.Player
import ru.kolkhoz.domain.model.PlayerId
import ru.kolkhoz.domain.repository.GameRepository
import ru.kolkhoz.domain.rules.GameRules
import ru.kolkhoz.ui.model.NewGameUiState
import ru.kolkhoz.ui.util.toHumanMessage

private const val MIN_PLAYERS = 3
private const val MAX_PLAYERS = 8
private const val MAX_NAME_LENGTH = 32

/**
 * ViewModel экрана создания игры (UI_SPEC.md, раздел 3).
 *
 * Хранит количество игроков и имена, пересчитывает доступность
 * кнопки «НАЧАТЬ ИГРУ» и создаёт партию через [GameRules.createGame].
 * Ошибки валидации показываются человекочитаемым сообщением
 * ([toHumanMessage]).
 */
@HiltViewModel
class NewGameViewModel @Inject constructor(
    private val repository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewGameUiState())

    /** Состояние экрана создания игры. */
    val uiState: StateFlow<NewGameUiState> = _uiState.asStateFlow()

    /**
     * Меняет количество игроков (3..8): при увеличении добавляет
     * пустые поля, при уменьшении удаляет последние
     * (UI_SPEC.md, 3.4).
     */
    fun onPlayerCountChange(count: Int) {
        val target = count.coerceIn(MIN_PLAYERS, MAX_PLAYERS)
        _uiState.update { state ->
            val names = when {
                target > state.playerNames.size ->
                    state.playerNames + List(target - state.playerNames.size) { "" }
                target < state.playerNames.size ->
                    state.playerNames.take(target)
                else -> state.playerNames
            }
            state.copy(playerCount = target, playerNames = names)
                .recalculated()
        }
    }

    /** Меняет имя игрока (обрезая до 32 символов) и пересчитывает canStartGame. */
    fun onPlayerNameChange(index: Int, name: String) {
        _uiState.update { state ->
            if (index !in state.playerNames.indices) return@update state
            val names = state.playerNames.toMutableList()
            names[index] = name.take(MAX_NAME_LENGTH)
            state.copy(playerNames = names).recalculated()
        }
    }

    /** Удаляет игрока по индексу; нельзя уменьшить ниже 3 игроков. */
    fun onDeletePlayer(index: Int) {
        _uiState.update { state ->
            if (state.playerNames.size <= MIN_PLAYERS) return@update state
            if (index !in state.playerNames.indices) return@update state
            val names = state.playerNames.toMutableList()
            names.removeAt(index)
            state.copy(playerCount = names.size, playerNames = names).recalculated()
        }
    }

    /**
     * Создаёт партию: [GameRules.createGame] → сохранение в
     * репозиторий → [onSuccess] с id партии.
     *
     * При ошибке валидации или сохранения — человекочитаемое
     * сообщение в `errorMessage`.
     *
     * @param onSuccess Вызывается после успешного сохранения.
     */
    fun onStartGame(onSuccess: (GameId) -> Unit) {
        val names = _uiState.value.playerNames
        val players = names.mapIndexed { index, name ->
            Player(
                id = PlayerId(UUID.randomUUID().toString()),
                name = name,
                position = index,
            )
        }
        val result = GameRules.createGame(
            id = GameId(UUID.randomUUID().toString()),
            createdAtMillis = System.currentTimeMillis(),
            players = players,
        )

        val game = result.getOrElse { error ->
            _uiState.update { it.copy(errorMessage = error.toHumanMessage()) }
            return
        }

        viewModelScope.launch {
            try {
                repository.saveGame(game)
                onSuccess(game.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Не удалось сохранить партию") }
            }
        }
    }

    /** Сбрасывает сообщение об ошибке. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Пересчитывает canStartGame по текущим именам. */
    private fun NewGameUiState.recalculated(): NewGameUiState =
        copy(canStartGame = playerNames.allValidUnique())

    /** Имена валидны по UI_SPEC.md 3.4: не пустые, уникальные без учёта регистра и пробелов. */
    private fun List<String>.allValidUnique(): Boolean {
        if (any { it.isBlank() || it.length > MAX_NAME_LENGTH }) return false
        val keys = map { it.trim().lowercase() }
        return keys.toSet().size == keys.size
    }
}
