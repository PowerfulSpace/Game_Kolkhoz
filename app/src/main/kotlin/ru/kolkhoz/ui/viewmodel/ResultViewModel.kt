package ru.kolkhoz.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kolkhoz.domain.model.GameId
import ru.kolkhoz.domain.repository.GameRepository
import ru.kolkhoz.ui.mapper.toResultUiState
import ru.kolkhoz.ui.model.ResultUiState

/**
 * ViewModel экрана итогов (UI_SPEC.md, раздел 6).
 *
 * `gameId` приходит из аргументов маршрута `result/{gameId}`
 * через [SavedStateHandle] — NavHost кладёт его туда автоматически.
 * Партия загружается один раз ([GameRepository.loadGame]) и
 * маппится в итоговую таблицу.
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: GameRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** id завершённой партии из аргументов маршрута. */
    private val gameId: GameId = GameId(
        checkNotNull(savedStateHandle.get<String>("gameId")) {
            "gameId is required for ResultScreen"
        },
    )

    private val _uiState = MutableStateFlow(
        ResultUiState(players = emptyList(), isLoading = true),
    )

    /** Состояние экрана итогов; пусто, если партия не найдена. */
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val game = repository.loadGame(gameId)
            _uiState.value = game?.toResultUiState()
                ?: ResultUiState(players = emptyList(), isLoading = false)
        }
    }

    /** Сбрасывает сообщение об ошибке. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
