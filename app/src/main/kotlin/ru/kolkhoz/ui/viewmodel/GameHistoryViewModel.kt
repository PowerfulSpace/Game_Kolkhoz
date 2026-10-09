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
import ru.kolkhoz.domain.repository.GameRepository
import ru.kolkhoz.ui.mapper.toGameHistoryItemUi
import ru.kolkhoz.ui.model.GameHistoryUiState

private const val LOAD_ERROR_MESSAGE = "Не удалось загрузить историю"

/**
 * ViewModel экрана «История игр» — списка сыгранных партий.
 *
 * Наблюдает за всеми партиями ([GameRepository.observeAllGames],
 * свежие сверху — порядок из контракта репозитория) и маппит
 * в [GameHistoryUiState].
 */
@HiltViewModel
class GameHistoryViewModel @Inject constructor(
    private val repository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameHistoryUiState(games = emptyList(), isEmpty = true),
    )

    /** Состояние экрана «История игр». */
    val uiState: StateFlow<GameHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeAllGames().collect { games ->
                    val items = games.map { it.toGameHistoryItemUi() }
                    _uiState.value = GameHistoryUiState(
                        games = items,
                        isEmpty = items.isEmpty(),
                    )
                }
            } catch (e: Exception) {
                // Ошибка Flow (например, БД) не должна ронять приложение;
                // показываем человекоческое сообщение (DESIGN_SYSTEM.md 14).
                _uiState.value = GameHistoryUiState(
                    games = emptyList(),
                    isEmpty = true,
                    errorMessage = LOAD_ERROR_MESSAGE,
                )
            }
        }
    }

    /** Сбрасывает сообщение об ошибке. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
