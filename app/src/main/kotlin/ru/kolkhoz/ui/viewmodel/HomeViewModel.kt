package ru.kolkhoz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    /** Состояние главного экрана. */
    val uiState: StateFlow<HomeUiState> = repository
        .observeActiveGame()
        .map { game -> HomeUiState(hasActiveGame = game != null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(hasActiveGame = false, isLoading = true),
        )
}
