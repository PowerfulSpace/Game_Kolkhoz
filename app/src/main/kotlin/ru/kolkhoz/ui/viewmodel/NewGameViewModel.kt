package ru.kolkhoz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

private const val EMPTY_NAME_ERROR = "Введите имя игрока"
private const val DUPLICATE_NAME_ERROR = "Такое имя уже есть"
private const val LONG_NAME_ERROR = "Имя слишком длинное"

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

    /** Флаг сохранения (защита от дабл-тапа «НАЧАТЬ ИГРУ»). */
    private var isSaving = false

    /**
     * Проверяет, есть ли активная партия. Используется
     * экраном перед созданием новой (UI_SPEC.md 3.4, 8.2).
     */
    suspend fun hasActiveGame(): Boolean {
        return repository.observeActiveGame().first() != null
    }

    /**
     * Завершает активную партию (если есть).
     * Вызывается перед созданием новой (UI_SPEC.md 8.2).
     */
    suspend fun finishActiveGame() {
        val active = repository.observeActiveGame().first() ?: return
        repository.saveGame(GameRules.finishGame(active))
    }

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
        if (isSaving) return
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

        isSaving = true
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.saveGame(game)
                onSuccess(game.id)
            } catch (e: Exception) {
                isSaving = false
                _uiState.update {
                    it.copy(errorMessage = "Не удалось сохранить партию", isSaving = false)
                }
            }
        }
    }

    /** Сбрасывает сообщение об ошибке. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Пересчитывает [NewGameUiState.nameErrors] для всех
     * игроков и [NewGameUiState.canStartGame] (UI_SPEC.md 3.5).
     */
    private fun NewGameUiState.recalculated(): NewGameUiState {
        val errors = validateNames(playerNames)
        return copy(
            nameErrors = errors,
            canStartGame = errors.all { it == null },
        )
    }

    /**
     * Inline-ошибки всех полей; дубликат — ошибка у обоих
     * игроков с одинаковым именем (сравнение trim + lowercase).
     */
    private fun validateNames(names: List<String>): List<String?> {
        val keys = names.map { it.trim().lowercase() }
        return names.mapIndexed { index, name ->
            when {
                name.isBlank() -> EMPTY_NAME_ERROR
                name.length > MAX_NAME_LENGTH -> LONG_NAME_ERROR
                keys.count { it == keys[index] } > 1 -> DUPLICATE_NAME_ERROR
                else -> null
            }
        }
    }
}
