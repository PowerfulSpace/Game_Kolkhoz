markdown
# ARCHITECTURE.md — архитектура приложения «Колхоз»

Этот документ описывает **техническую архитектуру** проекта: модули, 
слои, зависимости, технологии. Правила игры — в `DOMAIN_MODEL.md`. 
Продукт — в `SPEC.md`.

---

## 1. Обзор

«Колхоз» — **многомодульное Android-приложение** на Kotlin с 
Jetpack Compose. Работает **оффлайн**, хранит данные локально 
(Room). Не имеет backend в MVP, но архитектура **позволяет** 
добавить его позже без переписывания domain и UI.

**Главный архитектурный принцип:**

> **Domain — центр.** Все зависят от domain. Domain не зависит 
> ни от кого.

---

## 2. Модули
Game_Kolkhoz/
├── app/ — Android-приложение: UI, Compose, навигация, DI
├── domain/ — чистая бизнес-логика (Kotlin/JVM, БЕЗ Android)
├── data/ — Room, репозитории (Android Library)
└── docs/ — документация

text

### 2.1. `domain`

- **Тип:** `kotlin("jvm")` — чистый Kotlin, без Android.
- **Содержит:** модели, правила (`GameRules`), инварианты.
- **Зависит от:** ничего (кроме `kotlinx-coroutines-core`, и то 
  пока не используется).
- **Кто зависит от него:** `app`, `data`.

### 2.2. `data`

- **Тип:** `com.android.library`.
- **Содержит:** Room-сущности, DAO, мапперы, реализации 
  репозиториев.
- **Зависит от:** `domain`.
- **Кто зависит от него:** `app`.

### 2.3. `app`

- **Тип:** `com.android.application`.
- **Содержит:** Compose UI, ViewModel, навигацию, DI-модули, тему.
- **Зависит от:** `domain`, `data`.

---

## 3. Слои

Внутри модулей — **слоистая архитектура**:
┌──────────────────────────────────────────┐
│ UI (Compose) │ ← app/
├──────────────────────────────────────────┤
│ ViewModel │ ← app/
├──────────────────────────────────────────┤
│ Repository │ ← data/ (интерфейс — domain/)
├──────────────────────────────────────────┤
│ Domain (GameRules) │ ← domain/
├──────────────────────────────────────────┤
│ Data (Room, DAO) │ ← data/
└──────────────────────────────────────────┘

text

**Поток данных:**

- **Сверху вниз:** UI → ViewModel → Repository → Domain / Data.
- **Снизу вверх:** Room → Repository → ViewModel → UI (через 
  `Flow` / `StateFlow`).

**Зависимости слоёв:**

- UI зависит от ViewModel.
- ViewModel зависит от Repository.
- Repository зависит от Domain и Data.
- Domain не зависит ни от чего.
- Data зависит только от Domain (модели).

---

## 4. Технологии

| Что | Чем |
|-----|-----|
| Язык | Kotlin (последняя стабильная) |
| UI | Jetpack Compose + Material 3 |
| Навигация | Navigation Compose |
| DI | Hilt |
| Локальная БД | Room |
| Кодогенерация | KSP |
| Асинхронность | Coroutines + Flow |
| Тесты | JUnit5 |
| Сборка | Gradle (Kotlin DSL) + Version Catalog |

**AGP 9.0+:** плагин `org.jetbrains.kotlin.android` **не 
применяется** — AGP содержит встроенную поддержку Kotlin. Это 
не обход, а официальный путь для AGP 9+.

**minSdk:** 26 (Android 8.0).
**targetSdk / compileSdk:** последний стабильный.

---

## 5. Модуль `domain` — детали

### 5.1. Структура
domain/
└── src/
├── main/kotlin/ru/kolkhoz/domain/
│ ├── model/ — Player, Game, GameEvent, GameState, ...
│ └── rules/ — GameRules
└── test/kotlin/ru/kolkhoz/domain/
├── GameRulesCreateTest.kt
├── GameRulesPocketTest.kt
├── GameRulesStreakTest.kt
├── GameRulesMissTest.kt
├── GameRulesCycleTest.kt
├── GameRulesUndoTest.kt
├── GameRulesRecomputeTest.kt
├── GameRulesFinishTest.kt
└── SanityTest.kt

text

### 5.2. Правила

- **Никаких Android-импортов.**
- **Никаких `System.currentTimeMillis()`** — время приходит 
  параметром.
- **Никакого `Random`.**
- **Никаких suspend-функций, КРОМЕ `GameRepository`.** 
  Интерфейс репозитория (`domain/repository/GameRepository.kt`) — 
  единственное место в domain, где допустимы suspend и `Flow`: 
  это контракт с внешним миром (persistence), а не правила игры. 
  `GameRules` остаётся чистой и синхронной.
- **Все модели — immutable.**
- **Все функции — чистые.**

### 5.3. Публичный API

Единственная точка входа: `GameRules`. Модели — публичные data 
class-ы. Никакой скрытой логики в моделях.

---

## 6. Модуль `data` — детали

### 6.1. Структура
data/
└── src/
    ├── main/kotlin/ru/kolkhoz/data/
    │   ├── db/
    │   │   ├── KolkhozDatabase.kt
    │   │   ├── entity/
    │   │   │   ├── GameEntity.kt
    │   │   │   ├── PlayerEntity.kt
    │   │   │   └── GameEventEntity.kt
    │   │   └── dao/
    │   │       ├── GameDao.kt
    │   │       ├── PlayerDao.kt
    │   │       └── EventDao.kt
    │   ├── mapper/
    │   │   ├── GameMapper.kt
    │   │   ├── PlayerMapper.kt
    │   │   └── EventMapper.kt
    │   ├── repository/
    │   │   └── LocalGameRepository.kt
    │   └── di/
    │       ├── DatabaseModule.kt
    │       └── RepositoryModule.kt
    └── test/kotlin/ru/kolkhoz/data/
        ├── mapper/        (JUnit5)
        ├── db/            (JUnit4 + Robolectric)
        └── repository/    (JUnit4 + Robolectric)

text

### 6.2. Правила

- **Никакой бизнес-логики.** Только хранение и маппинг.
- **Room-сущности ≠ domain-модели.** Отдельные классы, мапперы 
  между ними.
- **Repository — интерфейс в `domain`, реализация в `data`.** Это 
  позволит позже добавить `RemoteGameRepository` без изменения 
  domain.
- **Все операции — suspend или Flow.**

### 6.3. Схема БД (черновик)

Три таблицы:

- `games` — `id`, `createdAtMillis`, `status`.
- `players` — `id`, `gameId`, `name`, `position`, `listOrder`.
- `game_events` — `id`, `gameId`, `sequenceNumber`, `playerId`, 
  `result`, `timestampMillis`.

**Поля `players`:** `position` — метаданные из domain (в правилах 
не участвуют, хранятся для буквального round-trip); `listOrder` — 
порядок игрока в `Game.players` (0..N-1), по нему `ORDER BY` при 
загрузке: цикл GameRules строится по порядку списка, а не по 
position.

Связи: `games 1—N players`, `games 1—N game_events`.

**Индексы:** `game_events(gameId, sequenceNumber)`.

**Схема уточняется при реализации Phase 2.**

---

## 7. Модуль `app` — детали

### 7.1. Структура (план)
app/
└── src/main/kotlin/ru/kolkhoz/
├── KolkhozApplication.kt
├── MainActivity.kt
├── navigation/
│ └── KolkhozNavHost.kt
├── di/
│ ├── RepositoryModule.kt
│ └── DatabaseModule.kt
├── ui/
│ ├── theme/ — тема, цвета, типографика
│ ├── components/ — переиспользуемые компоненты
│ ├── home/ — главный экран
│ ├── newgame/ — создание игры
│ ├── game/ — игровой экран
│ ├── history/ — история ударов
│ └── result/ — итоги
└── viewmodel/
├── HomeViewModel.kt
├── NewGameViewModel.kt
├── GameViewModel.kt
└── HistoryViewModel.kt

text

### 7.2. Правила

- **UI не считает очки.** Только отображает `GameState` из 
  ViewModel.
- **UI не вызывает `GameRules` напрямую.** Только через ViewModel.
- **ViewModel — тонкая.** Оркестрирует вызовы Repository, 
  преобразует в UI-state.
- **Compose-экраны — stateless.** Получают state и callbacks.
- **Навигация — через `NavHost`.** Один граф.

### 7.3. ViewModel

Каждый экран — своя ViewModel:

- `HomeViewModel` — список партий, продолжить незавершённую.
- `NewGameViewModel` — количество игроков, имена, валидация, 
  создание.
- `GameViewModel` — текущая партия, `applyShot`, `undoLastShot`, 
  `finishGame`.
- `HistoryViewModel` — история ударов (может быть частью 
  `GameViewModel`).

**ViewModel возвращает UI-state:**

```kotlin
data class GameUiState(
    val players: List<PlayerUi>,
    val currentPlayerId: PlayerId,
    val currentStreak: Int,
    val status: GameStatus,
    // ...
)
Domain-модели не уходят в UI напрямую, если это не нужно.
Обычно — маппинг в UI-модели для удобства Compose.

8. Dependency Rule
Главное правило:

Зависимости направлены к domain. Domain не знает ни о ком.

text
app ──────▶ domain
 │            ▲
 │            │
 ▼            │
data ─────────┘
Что это даёт:

Domain можно тестировать без Android.

Domain можно переиспользовать (KMP, backend на Kotlin, что
угодно).

Domain не сломается при смене UI или БД.

Логика игры — в одном месте, не размазана.

Нарушения, которые запрещены:

❌ domain импортирует что-либо из app или data.

❌ domain импортирует Android-классы.

❌ data импортирует что-либо из app.

❌ UI вызывает GameRules напрямую (только через ViewModel).

9. Offline-first
Приложение работает полностью оффлайн.

Нет сетевых запросов в MVP.

Нет разрешений на интернет в манифесте.

Все данные — локально (Room).

Все операции — синхронные (или suspend к Room).

На будущее (v2+):

Backend — опциональный.

Архитектура готова: GameRepository — интерфейс в domain,
реализации в data (LocalGameRepository,
RemoteGameRepository).

Синхронизация — отдельный слой, не трогает domain.

Domain не знает, откуда данные — из Room или из сети.

10. Дизайн-система (обзор)
Полностью — в DESIGN_SYSTEM.md и UI_SPEC.md.

В архитектуре:

Все токены дизайна — в app/ui/theme/.

Цвета, типографика, размеры — через CompositionLocal или
объекты.

Компоненты (KolhozButton, PlayerRow, ScoreText) — в
app/ui/components/.

Никаких «магических чисел» в Compose — только токены.

11. Обработка ошибок
11.1. Domain
Валидация — Result<T> + sealed GameError.

Программные ошибки — require / IllegalArgumentException.

Невалидные состояния — возврат без изменений.

(Подробно — в DOMAIN_MODEL.md, раздел 5.)

11.2. Data
Ошибки Room — оборачиваются в Result или пробрасываются как
Flow-ошибки.

Повреждённые данные — логируются, пользователю показывается
человеческое сообщение.

11.3. UI
Ошибки валидации — inline в полях ввода.

Критические ошибки — snackbar / диалог.

Тексты — человеческие, на русском.

Никаких stacktrace в UI.

12. Тестирование
12.1. Domain (unit-тесты)
JUnit5.

Чистые функции — легко тестировать.

Минимум 20 тестов (см. DOMAIN_MODEL.md, раздел 7).

12.2. Data (integration-тесты)
Room in-memory DB.

Проверка DAO, мапперов, репозитория.

Room-тесты используют Robolectric (JUnit4) + junit-vintage-engine, 
чтобы работать в :data:test без эмулятора. Мапперы — JUnit5.
Разделение зафиксировано в AGENTS.md §6.4.

12.3. UI (instrumented-тесты)
Compose UI tests.

Основные сценарии: создать игру → забить → undo → завершить.

Не покрывают всё — только critical path.

12.4. Ручное тестирование
На реальном устройстве.

Особенно: игровой экран в portrait и landscape.

При 3, 4, 8 игроках.

13. Что НЕ делаем в MVP
Осознанно не входит в архитектуру первой версии:

❌ Backend (FastAPI, PostgreSQL, что угодно).

❌ Сеть (Retrofit, Ktor).

❌ Авторизация (JWT, OAuth).

❌ Sync между устройствами.

❌ Multiplayer.

❌ Аналитика (Firebase, что угодно).

❌ Crash reporting (в MVP — только логи).

❌ Push-уведомления.

❌ Deep links.

❌ Виджеты.

❌ Wear OS, TV, Auto.

Всё это — возможно в v2+, архитектура позволяет, но
сейчас — нет.

14. Диаграмма зависимостей (итоговая)
text
┌─────────────────────────────────────┐
│              app                    │
│  ┌───────────────────────────────┐  │
│  │  UI (Compose)                 │  │
│  │      │                        │  │
│  │      ▼                        │  │
│  │  ViewModel                    │  │
│  │      │                        │  │
│  └──────┼────────────────────────┘  │
└─────────┼───────────────────────────┘
          │
          ▼
┌─────────────────────────────────────┐
│              data                   │
│  ┌───────────────────────────────┐  │
│  │  RepositoryImpl               │  │
│  │      │                        │  │
│  │      ▼                        │  │
│  │  Room (DAO, Entity)           │  │
│  └───────────────────────────────┘  │
└─────────┬───────────────────────────┘
          │
          ▼
┌─────────────────────────────────────┐
│             domain                  │
│                                     │
│  GameRules, Game, GameState, ...    │
│                                     │
│  (никаких Android-зависимостей)     │
└─────────────────────────────────────┘
Стрелки — направление зависимостей. app зависит от data
и domain. data зависит от domain. domain не зависит ни от
кого.

15. Roadmap архитектуры
Phase	Что	Модули
1	Domain + тесты	domain
2	Room, Repository	data
3	UI (Compose)	app
4	Связывание, ViewModel, навигация	app
5	Полировка, ошибки, release	все
Подробно — в ROADMAP.md.

16. Ссылки
AGENTS.md — правила работы AI-агента.

SPEC.md — спецификация продукта.

DOMAIN_MODEL.md — модели, правила, инварианты.

DESIGN_SYSTEM.md — токены дизайна.

UI_SPEC.md — экраны и состояния.

ROADMAP.md — фазы разработки.

TEST_PLAN.md — план тестирования.