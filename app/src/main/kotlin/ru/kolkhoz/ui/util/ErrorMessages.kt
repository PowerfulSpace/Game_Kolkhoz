package ru.kolkhoz.ui.util

import ru.kolkhoz.domain.rules.GameError

/**
 * Человеческое (русское) сообщение для ошибки валидации партии.
 *
 * Тексты — по UI_SPEC.md (inline-подсказки и snackbar), не
 * технические описания из [GameError].
 */
fun GameError.toHumanMessage(): String = when (this) {
    is GameError.TooFewPlayers -> "Нужно минимум 3 игрока"
    is GameError.TooManyPlayers -> "Максимум 8 игроков"
    is GameError.EmptyName -> "Введите имя игрока"
    is GameError.DuplicateName -> "Такое имя уже есть в партии"
    is GameError.NameTooLong -> "Имя слишком длинное (макс. 32 символа)"
    is GameError.InvalidPositions -> "Ошибка создания партии"
}
