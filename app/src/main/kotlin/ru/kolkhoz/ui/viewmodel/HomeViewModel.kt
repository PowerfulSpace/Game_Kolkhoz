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
import ru.kolkhoz.ui.model.HomeUiState

/**
 * ViewModel главного экрана.
 *
 * Следит за активной партией: пока она есть — на главном
 * показывается кнопка «ПРОДОЛЖИТЬ ИГРУ» (UI_SPEC.md, 2.5).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(hasActiveGame = false, isLoading = true),
    )

    /** Состояние главного экрана. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeActiveGame().collect { game ->
                _uiState.update { it.copy(hasActiveGame = game != null, isLoading = false) }
            }
        }
    }

    /** Сбрасывает сообщение об ошибке. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
