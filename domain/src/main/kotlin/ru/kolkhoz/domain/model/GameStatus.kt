package ru.kolkhoz.domain.model

/**
 * Статус партии.
 */
enum class GameStatus {
    /** Партия идёт, удары принимаются. */
    ACTIVE,

    /** Партия завершена: новые удары игнорируются (см. [ru.kolkhoz.domain.rules.GameRules.applyShot]). */
    FINISHED,
}
