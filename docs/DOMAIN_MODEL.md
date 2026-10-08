markdown
# DOMAIN_MODEL.md — модель предметной области «Колхоз»

Этот документ описывает **бизнес-логику** приложения в терминах 
кода. Он — источник истины для модуля `domain`. Если код в 
`domain/` расходится с этим документом — **документ прав**, код 
надо исправить.

Правила игры с точки зрения игрока — в `SPEC.md`. Здесь — то же 
самое, но в терминах моделей, функций и инвариантов.

---

## 1. Принципы domain-слоя

1. **Чистота.** Domain не зависит ни от Android, ни от Room, ни 
   от UI, ни от сети. Только Kotlin/JVM.
2. **Иммутабельность.** Все модели — `data class` без мутабельных 
   полей. Функции возвращают **новые** объекты, не мутируют старые.
3. **Детерминированность.** Никаких `System.currentTimeMillis()`, 
   `Random`, обращений к БД или сети. Время приходит **параметром**.
4. **События — источник истины.** Счёт, текущий игрок, серия — 
   **производные** от истории событий. Не хранятся отдельно.
5. **Чистые функции.** Одна и та же `Game` + одно и то же событие 
   → всегда один и тот же результат.

---

## 2. Модели

### 2.1. `Player`

Игрок в партии.

```kotlin
data class Player(
    val id: PlayerId,
    val name: String,
    val position: Int,   // 0..N-1, фиксирован при создании партии
)
id — уникален внутри партии.

name — от 1 до N символов, уникален внутри партии.

position — определяет порядок хода. Не меняется после создания.

2.2. ShotResult
Результат удара.

kotlin
enum class ShotResult {
    POCKET,   // шар забит
    MISS,     // промах
}
2.3. GameStatus
Статус партии.

kotlin
enum class GameStatus {
    ACTIVE,     // идёт
    FINISHED,   // завершена
}
2.4. GameEvent
Один удар. Единственный источник истины о том, что произошло.

kotlin
data class GameEvent(
    val id: EventId,
    val sequenceNumber: Int,      // 1, 2, 3, ... — порядковый номер
    val playerId: PlayerId,       // кто сделал удар
    val result: ShotResult,
    val timestampMillis: Long,    // приходит извне, не System.currentTimeMillis()
)
sequenceNumber — сквозная нумерация от 1. Используется для
упорядочивания и для отображения в UI.

События не удаляются из середины. Undo удаляет только
последнее.

События иммутабельны.

2.5. Game
Партия. Хранимая сущность.

kotlin
data class Game(
    val id: GameId,
    val createdAtMillis: Long,
    val players: List<Player>,       // порядок = порядок хода
    val events: List<GameEvent>,     // все удары по порядку
    val status: GameStatus,
)
players — минимум 3, максимум 8.

events — может быть пустым (партия только создана).

status — единственное мутабельное «намерение». Меняется через
finishGame.

2.6. GameState
Производное состояние. Не хранится. Вычисляется из Game
через recompute.

kotlin
data class GameState(
    val players: List<Player>,
    val scores: Map<PlayerId, Int>,          // счёт каждого
    val currentPlayerId: PlayerId,           // чей ход
    val currentStreak: Int,                  // текущая серия (0 если нет)
    val streakPenaltyTargetId: PlayerId?,    // кому идёт штраф за текущую серию
    val lastShooterId: PlayerId?,            // кто сделал последний удар
    val status: GameStatus,
    val eventsCount: Int,                    // сколько событий применено
)
Ключевые поля:

currentPlayerId — чей ход сейчас. Меняется только при MISS.

currentStreak — сколько подряд забил текущий игрок без
промаха. Сбрасывается в 0 при MISS.

streakPenaltyTargetId — кому идёт −1 за текущую серию.
Устанавливается в начале серии (первый POCKET после MISS или
после начала партии) и не меняется до конца серии. При MISS
сбрасывается в null.

lastShooterId — тот, кто сделал последний удар (не важно,
POCKET или MISS). Используется для определения штрафа при
следующем POCKET, если серия началась заново.

2.7. Value-классы идентификаторов
kotlin
@JvmInline value class PlayerId(val value: String)
@JvmInline value class GameId(val value: String)
@JvmInline value class EventId(val value: String)
value class — zero-cost обёртка, даёт type safety.

Внутри — String (например, UUID). Формат — деталь реализации.

В тестах можно использовать простые строки (PlayerId("p1")).

3. Правила (объект GameRules)
Все функции — чистые, без side effects, без исключений
для бизнес-логики.

kotlin
object GameRules {

    fun createGame(
        id: GameId,
        createdAtMillis: Long,
        players: List<Player>,
    ): Result<Game>

    fun recompute(game: Game): GameState

    fun applyShot(
        game: Game,
        playerId: PlayerId,
        result: ShotResult,
        timestampMillis: Long,
    ): Game

    fun undoLastShot(game: Game): Game

    fun finishGame(game: Game): Game

    fun nextPlayerId(game: Game, currentPlayerId: PlayerId): PlayerId

    fun previousPlayerIdInCycle(game: Game, currentPlayerId: PlayerId): PlayerId
}
3.1. createGame
Создаёт партию. Валидирует входные данные.

Правила валидации:

players.size в диапазоне 3..8.

Все имена не пустые и не состоят из пробелов.

Все имена уникальны (без учёта регистра).

Длина имени — от 1 до 32 символов (уточняется).

position у игроков — уникальные, 0..N-1.

Возвращает: Result<Game>.

Success(game) — если всё валидно.

Failure(error) — если нет.

Ошибки — sealed class:

kotlin
sealed class GameError {
    data object TooFewPlayers : GameError()
    data object TooManyPlayers : GameError()
    data class EmptyName(val playerId: PlayerId) : GameError()
    data class DuplicateName(val name: String) : GameError()
    data class NameTooLong(val name: String, val maxLength: Int) : GameError()
    data class InvalidPosition(val playerId: PlayerId) : GameError()
}
Пример:

kotlin
val result = GameRules.createGame(
    id = GameId("g1"),
    createdAtMillis = 1_700_000_000_000L,
    players = listOf(
        Player(PlayerId("p1"), "Саша", 0),
        Player(PlayerId("p2"), "Петя", 1),
        Player(PlayerId("p3"), "Коля", 2),
    ),
)
// result = Success(Game(...))
3.2. recompute
Главная функция. Пересчитывает состояние из событий.

Алгоритм:

Начальное состояние:

scores — все нули.

currentPlayerId — первый игрок (players[0].id).

currentStreak = 0.

streakPenaltyTargetId = null.

lastShooterId = null.

status = game.status.

eventsCount = 0.

Для каждого события по порядку:

Если MISS:

Счёт не меняется.

currentStreak = 0.

streakPenaltyTargetId = null.

lastShooterId = event.playerId.

currentPlayerId = nextPlayerId(currentPlayerId).

eventsCount += 1.

Если POCKET:

Определяем получателя штрафа (penaltyTarget):

Если streakPenaltyTargetId != null (серия продолжается)
→ penaltyTarget = streakPenaltyTargetId.

Иначе (начинается новая серия):

Если lastShooterId == null (первое событие партии)
→ penaltyTarget = previousPlayerIdInCycle(currentPlayerId).

Иначе → penaltyTarget = lastShooterId.

streakPenaltyTargetId = penaltyTarget.

scores[event.playerId] += 1.

scores[penaltyTarget] -= 1.

currentStreak += 1.

lastShooterId = event.playerId.

currentPlayerId не меняется (забивший продолжает бить).

eventsCount += 1.

Возврат GameState.

Свойства:

Чистая. Не трогает game, возвращает новый GameState.

Идемпотентная. recompute(game) == recompute(game).

Детерминированная. Один и тот же game → один и тот же
результат.

Никогда не падает. Пустые events — валидный начальный стейт.

Пример 1 — одиночный забитый:

text
Игроки: [Саша, Петя, Коля, Дима]
События: [
  1. Петя POCKET
]

Результат:
  currentPlayerId = Петя (продолжает бить)
  scores = { Саша: -1, Петя: +1, Коля: 0, Дима: 0 }
  currentStreak = 1
  streakPenaltyTargetId = Саша
  lastShooterId = Петя
Пояснение: первое событие партии — не «первый ход», а
первое событие. lastShooterId == null → penaltyTarget = previousPlayerIdInCycle(Петя) = Саша.

Пример 2 — серия:

text
События: [
  1. Саша MISS
  2. Петя POCKET
  3. Петя POCKET
  4. Петя POCKET
  5. Петя MISS
  6. Коля POCKET
]

Результат:
  scores = { Саша: -3, Петя: +2, Коля: +1, Дима: 0 }
  currentPlayerId = Коля (продолжает бить)
  currentStreak = 1
  streakPenaltyTargetId = Петя
  lastShooterId = Коля
Пояснение:

Событие 2: серия начинается. lastShooterId = Саша (после MISS)
→ штраф Саше. streakPenaltyTargetId = Саша.

События 3, 4: серия продолжается. Штраф Саше (через
streakPenaltyTargetId).

Событие 5: MISS, серия сброшена. streakPenaltyTargetId = null.

Событие 6: новая серия. lastShooterId = Петя (после MISS) →
штраф Пете. streakPenaltyTargetId = Петя.

3.3. applyShot
Применяет удар. Возвращает новую Game с добавленным событием.

Порядок проверок:

Если game.status == FINISHED → вернуть game без
изменений. (События завершённой партии не меняются.)

state = recompute(game).

require(playerId == state.currentPlayerId) → иначе
IllegalArgumentException. Это программная ошибка UI: UI
всегда знает currentPlayerId. Если он передал не того — баг
в UI.

Создать новое GameEvent:

id — новый EventId.

sequenceNumber = game.events.size + 1.

playerId, result, timestampMillis — из аргументов.

Вернуть game.copy(events = game.events + newEvent).

KDoc-контракт:

kotlin
/**
 * Применяет удар.
 *
 * @param playerId должен совпадать с currentPlayerId из 
 *   recompute(game). Иначе — IllegalArgumentException.
 * @throws IllegalArgumentException если playerId != currentPlayerId. 
 *   Это программная ошибка UI, не бизнес-сценарий.
 *
 * Если game.status == FINISHED — возвращает game без изменений.
 */
fun applyShot(...): Game
3.4. undoLastShot
Удаляет последнее событие. Пересчёт делается при следующем
recompute.

Поведение:

Если game.events.isEmpty() → вернуть game без изменений.

Иначе → game.copy(events = game.events.dropLast(1)).

KDoc-контракт:

kotlin
/**
 * Отменяет последний удар.
 *
 * Если событий нет — возвращает game без изменений.
 *
 * Undo реализуется как удаление последнего события + пересчёт 
 * (пересчёт делается вызывающей стороной через recompute).
 * Undo НЕ реализуется как ручное изменение счёта.
 */
fun undoLastShot(game: Game): Game
3.5. finishGame
Меняет статус на FINISHED.

Поведение:

Если уже FINISHED → вернуть game без изменений (идемпотентно).

Иначе → game.copy(status = GameStatus.FINISHED).

3.6. nextPlayerId
Возвращает следующего игрока по циклу.

Пример: игроки [Саша, Петя, Коля, Дима].

nextPlayerId(Саша) = Петя

nextPlayerId(Дима) = Саша

Precondition: currentPlayerId есть в game.players.
Нарушение — программная ошибка, require.

3.7. previousPlayerIdInCycle
Возвращает предыдущего игрока по циклу.

Пример: игроки [Саша, Петя, Коля, Дима].

previousPlayerIdInCycle(Саша) = Дима

previousPlayerIdInCycle(Петя) = Саша

Используется только для определения штрафа при первом
событии партии.

4. Инварианты
Следующие утверждения всегда истинны для любой валидной Game
и её GameState:

players.size в диапазоне 3..8.

Все position уникальны и в диапазоне 0..players.size-1.

Все имена уникальны (без учёта регистра).

events упорядочены по sequenceNumber (1, 2, 3, ...).

events[i].sequenceNumber == i + 1.

events[i].playerId есть среди players.

Сумма всех счетов = 0. Каждый POCKET даёт +1 и −1
одновременно.

currentPlayerId есть среди players.

currentStreak >= 0.

Если currentStreak > 0, то streakPenaltyTargetId != null.

Если currentStreak == 0, то streakPenaltyTargetId == null.

status == FINISHED → applyShot не меняет game.

recompute идемпотентна.

Инвариант 7 (сумма = 0) — самый важный. Он ловит баги в
правилах: если сумма счетов ≠ 0, значит либо +1 без −1, либо
наоборот. Такого быть не может.

Проверка инвариантов — в тестах. В коде — не проверяем
(дорого), но в тестах — обязательно.

5. Обработка ошибок
Функция	Возвращает	Ошибки
createGame	Result<Game>	GameError.* — валидация
applyShot	Game	IllegalArgumentException при playerId != currentPlayerId (программная ошибка)
undoLastShot	Game	Нет. Пустые events → без изменений
finishGame	Game	Нет. Идемпотентно
recompute	GameState	Нет. Никогда не падает
nextPlayerId	PlayerId	require при невалидном currentPlayerId
previousPlayerIdInCycle	PlayerId	require при невалидном currentPlayerId
Принципы:

Валидация пользовательского ввода — Result + sealed
GameError. Это ожидаемые сценарии.

Программные ошибки (нарушение инварианта) — require /
IllegalArgumentException. Это баги, не сценарии.

Невалидные состояния (FINISHED, пустые events) — возврат
без изменений. Это защита, не ошибка.

6. Нейминг
Термин	Значение
Player	Игрок
PlayerId	Идентификатор игрока
Game	Партия (хранимая)
GameState	Состояние партии (производное)
GameEvent	Один удар
ShotResult	Результат удара (POCKET / MISS)
GameStatus	Статус партии (ACTIVE / FINISHED)
lastShooterId	Тот, кто сделал последний удар
currentPlayerId	Чей ход сейчас
currentStreak	Текущая серия подряд забитых
streakPenaltyTargetId	Кому идёт штраф за текущую серию
sequenceNumber	Порядковый номер события
Правило: lastShooterId ≠ currentPlayerId. Первый — «кто
бил последним», второй — «чей ход сейчас». После POCKET они
совпадают. После MISS — нет.

7. Тесты
Обязательное покрытие:

createGame: 3, 4, 8 игроков; отказ при <3, >8, пустых именах,
дубликатах, невалидной длине.

recompute / applyShot: одиночные POCKET, серии (2, 3, 5
подряд), MISS, MISS подряд, первый удар партии, циклический
порядок при 3, 4, 8 игроках, отрицательный счёт.

Пример из SPEC (раздел 4.5) — обязательно.

undoLastShot: после POCKET, после серии, после MISS, на
пустой игре, несколько undo подряд, undo + повторный удар.

finishGame: смена статуса, идемпотентность.

applyShot на FINISHED игре — возврат без изменений.

applyShot с неверным playerId — IllegalArgumentException.

Инвариант «сумма счетов = 0» — проверять в каждом тесте, где
есть события.

Минимум: 20 тестов.

8. Что НЕ делает domain
Не работает с БД.

Не работает с UI.

Не работает с сетью.

Не знает про Android.

Не хранит счёт как отдельное поле.

Не использует System.currentTimeMillis().

Не использует Random.

Не мутирует входные данные.

Не бросает исключений для бизнес-логики.