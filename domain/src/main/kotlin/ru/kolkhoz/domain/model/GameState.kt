package ru.kolkhoz.domain.model

/**
 * Производное состояние партии.
 *
 * Ни одно поле не хранится как источник истины: состояние всегда пересчитывается
 * из списка событий функцией `GameRules.recompute(game)`.
 * Поэтому undo — это удаление последнего события + пересчёт, а не ручная правка счёта.
 *
 * @property players игроки партии в порядке хода
 * @property scores текущий счёт каждого игрока; отрицательные значения разрешены
 * @property currentPlayerId игрок, чей сейчас ход
 * @property currentStreak длина текущей серии подряд забитых шаров текущим игроком
 * @property lastShooterId кто бил последним; `null` только пока событий нет
 * @property streakPenaltyTargetId кому идёт штраф за текущую серию; `null`, если серии нет.
 *   Поле предложено в спецификации (Part 3) и добавлено в [GameState]: без него
 *   при серии забитых подряд невозможно однозначно определить, кому начислять −1,
 *   кроме как повторно «отматывая» события.
 * @property status статус партии
 * @property eventsCount число уже применённых событий
 */
data class GameState(
    val players: List<Player>,
    val scores: Map<PlayerId, Int>,
    val currentPlayerId: PlayerId,
    val currentStreak: Int,
    val lastShooterId: PlayerId?,
    val streakPenaltyTargetId: PlayerId?,
    val status: GameStatus,
    val eventsCount: Int,
)
