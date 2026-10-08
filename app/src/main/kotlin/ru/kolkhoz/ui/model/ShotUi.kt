package ru.kolkhoz.ui.model

/**
 * Удар для отображения в истории (UI-модель).
 *
 * Уже преобразован из [ru.kolkhoz.domain.model.GameEvent]:
 * имя игрока разрешено в строку, время отформатировано в «HH:mm».
 *
 * @property sequenceNumber Порядковый номер удара в партии.
 * @property playerName Имя игрока, сделавшего удар.
 * @property isPocket Забил ли шар.
 * @property time Уже отформатированное время, например `21:34`.
 */
data class ShotUi(
    val sequenceNumber: Int,
    val playerName: String,
    val isPocket: Boolean,
    val time: String,
)
