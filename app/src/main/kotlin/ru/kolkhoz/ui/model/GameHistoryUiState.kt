package ru.kolkhoz.ui.model

/**
 * Состояние экрана «История игр» — список сыгранных партий.
 *
 * @property games Список партий (свежие сверху).
 * @property isEmpty Партий нет.
 * @property errorMessage Ошибка загрузки.
 */
data class GameHistoryUiState(
    val games: List<GameHistoryItemUi>,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Одна партия в списке истории игр.
 *
 * @property gameId id партии (для перехода к результату).
 * @property date Дата партии в формате "08.10.2026".
 * @property playersCount Количество игроков.
 * @property shotsCount Количество ударов.
 * @property players Список игроков с их счётом (уже
 *   отсортирован по счёту).
 */
data class GameHistoryItemUi(
    val gameId: String,
    val date: String,
    val playersCount: Int,
    val shotsCount: Int,
    val players: List<PlayerResultUi>,
)
