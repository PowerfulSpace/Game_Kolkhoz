package ru.kolkhoz.ui.model

/**
 * Состояние экрана создания игры (UI_SPEC.md, раздел 3).
 *
 * @property playerCount Текущее количество игроков (3..8);
 *   всегда равно размеру [playerNames].
 * @property playerNames Имена игроков, по элементу на игрока.
 * @property canStartGame Можно ли начать игру: все имена
 *   валидны (не пустые, уникальные без учёта регистра,
 *   1–32 символа) — UI_SPEC.md 3.4.
 * @property nameErrors Inline-ошибки имён: по элементу на
 *   игрока; `null` — ошибки нет, иначе текст подсказки
 *   (UI_SPEC.md 3.5).
 * @property isSaving Идёт ли сохранение новой партии; пока
 *   `true` — кнопка «НАЧАТЬ ИГРУ» заблокирована
 *   (защита от дабл-тапа).
 * @property wasSubmitted Была ли попытка «НАЧАТЬ ИГРУ»; пока
 *   `false` — inline-ошибки имён не показываются.
 * @property errorMessage Человеческое сообщение об ошибке
 *   создания; `null` — ошибки нет.
 */
data class NewGameUiState(
    val playerCount: Int = 4,
    val playerNames: List<String> = List(4) { "" },
    val canStartGame: Boolean = false,
    val nameErrors: List<String?> = List(4) { null },
    val isSaving: Boolean = false,
    val wasSubmitted: Boolean = false,
    val errorMessage: String? = null,
)
