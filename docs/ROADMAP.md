markdown
# ROADMAP.md — план разработки «Колхоз»

Этот документ описывает **порядок** разработки приложения по 
фазам. Каждая фаза имеет чёткие границы и Definition of Done.

**Главное правило:**

> Агент работает **только в текущей фазе**. Не забегает вперёд, 
> не делает «на будущее». Если задача из следующей фазы — 
> остановиться.

---

## Обзор фаз

| Phase | Название | Модули | Статус |
|-------|----------|--------|--------|
| 0 | Каркас проекта | все | ✅ Завершена |
| 1 | Domain: модели, правила, тесты | `domain` | ✅ Завершена |
| 2 | Data: Room, Repository | `data` | ⏳ Ожидает |
| 3a | UI: тема + компоненты | `app` | ⏳ Ожидает |
| 3b | UI: экраны | `app` | ⏳ Ожидает |
| 4 | Связывание: ViewModel, навигация | `app` | ⏳ Ожидает |
| 5 | Полировка, release | все | ⏳ Ожидает |

---

## Phase 0 — Каркас проекта

**Цель:** подготовить многомодульный Android-проект, готовый к 
разработке.

**Что сделано:**

- [x] Многомодульная структура: `app`, `domain`, `data`, `docs`.
- [x] Version Catalog (`gradle/libs.versions.toml`).
- [x] Kotlin, Compose, Hilt, Room, KSP, JUnit5 подключены.
- [x] `minSdk = 26`, `targetSdk` / `compileSdk` — последние.
- [x] `domain` — чистый `kotlin("jvm")`, без Android.
- [x] `KolkhozApplication` + `MainActivity` (пустая).
- [x] JUnit5 работает (`SanityTest.kt`).
- [x] `.gitignore`, `README.md`, `LICENSE` на месте.

**Definition of Done:**

- [x] `./gradlew assembleDebug` — BUILD SUCCESSFUL.
- [x] `./gradlew test` — проходит.
- [x] APK собирается, label «Колхоз».
- [x] JUnit Platform работает (проверено реальным тестом).

**Отклонения, зафиксированные:**

- `org.jetbrains.kotlin.android` не применяется — AGP 9+ содержит 
  встроенный Kotlin.
- `compileSdk` / `targetSdk` = 37 — требование новых AndroidX / 
  Compose.

**Результат:** ✅ Завершена.

---

## Phase 1 — Domain

**Цель:** реализовать **всю бизнес-логику** «Колхоза» с полным 
покрытием unit-тестами.

**Модуль:** `domain`.

**Задачи:**

1. **Модели** (`domain/model/`):
   - `Player`, `PlayerId`
   - `GameEvent`, `EventId`
   - `Game`, `GameId`
   - `GameState`
   - `ShotResult` (enum)
   - `GameStatus` (enum)

2. **Правила** (`domain/rules/`):
   - `GameRules.createGame` — валидация, `Result<Game>`
   - `GameRules.recompute` — пересчёт состояния из событий
   - `GameRules.applyShot` — применение удара
   - `GameRules.undoLastShot` — откат последнего события
   - `GameRules.finishGame` — завершение партии
   - `GameRules.nextPlayerId` — следующий по циклу
   - `GameRules.previousPlayerIdInCycle` — предыдущий по циклу

3. **Ошибки:**
   - `GameError` — sealed class (валидация)
   - `Result<Game>` для `createGame`
   - `require` для программных ошибок

4. **Тесты** (`domain/test/`):
   - Минимум 20 тестов.
   - Покрытие: создание, одиночные `POCKET`, серии, `MISS`, цикл, 
     `undo`, `recompute`, `finishGame`, первый удар, отрицательный 
     счёт.
   - Пример из `SPEC.md` (раздел 4.5) — обязательно.
   - Инвариант «сумма счетов = 0» — проверять в каждом тесте.

**Definition of Done:**

- [x] Все модели и правила реализованы.
- [x] `./gradlew :domain:test` — BUILD SUCCESSFUL.
- [x] Минимум 20 тестов, все зелёные.
- [x] KDoc на всех публичных API.
- [x] Нет Android-импортов в `domain`.
- [x] Нет `System.currentTimeMillis()` в `domain`.
- [x] Нет `Random` в `domain`.
- [x] `SanityTest.kt` сохранён.
- [x] Код совпадает с `DOMAIN_MODEL.md`.

**Отклонения, зафиксированные:**

- `applyShot` возвращает `Game`, не `Result<Game>`. Некорректные 
  вызовы (FINISHED) → возврат без изменений.
- `applyShot` с неверным `playerId` → `IllegalArgumentException`. 
  Это программная ошибка UI.
- `streakPenaltyTargetId` добавлен в `GameState` для корректной 
  обработки серий.
- Имена уникальны без учёта регистра и пробелов (trim + lowercase).
- Ограничение длины имени: 1..32 символа.
- `GameError` включает: `TooFewPlayers(actual)`, 
  `TooManyPlayers(actual)`, `EmptyName(playerId)`, 
  `DuplicateName(name)`, `NameTooLong(name, maxLength)`, 
  `InvalidPositions(positions)`.
- `position` валидируется в `createGame`: уникальны, 0..N-1.

**Результат:** ✅ Завершена. 52 теста, все зелёные.

---

## Phase 2 — Data

**Цель:** реализовать **локальное хранение** партий в Room.

**Модуль:** `data`.

**Зависит от:** Phase 1 (нужны domain-модели).

**Задачи:**

0. **Интерфейс `GameRepository` в `domain/repository/`:**
   - `suspend fun saveGame(game: Game)`
   - `suspend fun loadGame(id: GameId): Game?`
   - `fun observeActiveGame(): Flow<Game?>`
   - `suspend fun loadAllGames(): List<Game>`
   - `suspend fun deleteGame(id: GameId)`
   - Финальный список методов — на этапе Phase 2, по мере 
     реализации. Здесь — ориентир.

1. **Room-сущности** (`data/db/entity/`):
   - `GameEntity`
   - `PlayerEntity`
   - `GameEventEntity`

2. **DAO** (`data/db/dao/`):
   - `GameDao` — CRUD партий
   - `EventDao` — CRUD событий

3. **База** (`data/db/`):
   - `KolkhozDatabase` — Room-база, версия 1
   - Миграции — пока не нужны (версия 1)

4. **Мапперы** (`data/mapper/`):
   - `GameMapper` — `GameEntity` ↔ `Game`
   - `PlayerMapper` — `PlayerEntity` ↔ `Player`
   - `EventMapper` — `GameEventEntity` ↔ `GameEvent`

5. **Repository:**
   - Интерфейс `GameRepository` — в `domain/repository/`. 
     Реализация `LocalGameRepository` — в `data/repository/`.

     Обоснование: Dependency Inversion. Domain определяет, что 
     ему нужно от хранилища, data — как это реализовано. Позволит 
     позже добавить `RemoteGameRepository` без изменения domain.

     ВАЖНО: `GameRepository` — единственное место в domain, где 
     допустимы suspend-функции и `Flow`. Это контракт с внешним 
     миром (persistence), а не правила игры. `GameRules` остаётся 
     без suspend.

6. **DI-модуль** (`data/di/`):
   - `DatabaseModule` — предоставляет `KolkhozDatabase`, DAO.
   - `RepositoryModule` — предоставляет `GameRepository`.

7. **Тесты** (`data/test/`):
   - Room in-memory DB.
   - Проверка DAO: сохранение, загрузка, обновление.
   - Проверка мапперов.
   - Проверка репозитория.

**Definition of Done:**

- [ ] Room-база создана.
- [ ] Все DAO работают.
- [ ] Все мапперы покрыты тестами.
- [ ] Repository реализован.
- [ ] Тесты проходят.
- [ ] Никакой бизнес-логики в `data`.
- [ ] `./gradlew :data:test` — BUILD SUCCESSFUL.
- [ ] `./gradlew assembleDebug` — BUILD SUCCESSFUL.

**Результат:** ⏳ Ожидает.

---

## Phase 3a — UI: тема + компоненты

**Цель:** реализовать дизайн-систему и все переиспользуемые 
компоненты. Без экранов.

**Модуль:** `app`.

**Зависит от:** Phase 1.

**Задачи:**

1. **Тема** (`app/ui/theme/`):
   - Цвета, типографика, отступы, радиусы — по `DESIGN_SYSTEM.md`.
   - Токены — в одном месте (`KolhozColors`, `KolhozTypography`, 
     `KolhozSpacing`, `KolhozRadius`).

2. **Компоненты** (`app/ui/components/`):
   - `KolhozButton`, `KolhozSecondaryButton`, `KolhozTextButton`
   - `PlayerRow`, `CurrentPlayerCard`
   - `ScoreText`, `SeriesIndicator`
   - `ShotHistoryRow`, `GameHistoryCard`
   - `KolhozDialog`, `KolhozTopBar`, `KolhozBottomBar`
   - `PlayerInput`, `PlayerCountSelector`

3. Compose Preview для каждого компонента.
4. Никаких экранов.

**Definition of Done:**

- [ ] Тема реализована по `DESIGN_SYSTEM.md`.
- [ ] Все компоненты реализованы.
- [ ] Compose Preview работает для каждого компонента.
- [ ] Нет «магических чисел» — только токены.
- [ ] `./gradlew assembleDebug` — BUILD SUCCESSFUL.

**Результат:** ⏳ Ожидает.

---

## Phase 3b — UI: экраны

**Цель:** собрать 5 экранов из компонентов Phase 3a.

**Модуль:** `app`.

**Зависит от:** Phase 3a.

**Задачи:**

1. `HomeScreen`.
2. `NewGameScreen`.
3. `GameScreen` (portrait + landscape).
4. `HistoryScreen`.
5. `ResultScreen`.
6. Ресурсы: `strings.xml`, иконки (vector drawable).
7. Compose UI tests — базовые сценарии.

**Definition of Done:**

- [ ] Все 5 экранов реализованы.
- [ ] GameScreen работает в portrait и landscape.
- [ ] Скриншоты совпадают с макетами.
- [ ] Compose UI tests проходят.
- [ ] `./gradlew assembleDebug` — BUILD SUCCESSFUL.

**Результат:** ⏳ Ожидает.

---

## Phase 4 — Связывание

**Цель:** связать UI с domain и data через ViewModel и навигацию.

**Модуль:** `app`.

**Зависит от:** Phase 3b.

**Задачи:**

1. **ViewModel:**
   - `HomeViewModel`
   - `NewGameViewModel`
   - `GameViewModel`
   - `HistoryViewModel` (если нужна отдельная)

2. **UI-state:**
   - `GameUiState`, `NewGameUiState`, `HomeUiState` — data class.
   - Маппинг из domain-моделей в UI-модели.

3. **Навигация:**
   - `KolkhozNavHost` — один граф.
   - Переходы: Home → NewGame → Game → History / Result.
   - «Продолжить» → сразу Game.
   - Back-stack: правильное поведение.

4. **DI-модули** (`app/di/`):
   - `RepositoryModule` — связывает интерфейс и реализацию.
   - `ViewModelModule` — если нужно.

**Definition of Done:**

- [ ] Все экраны работают с ViewModel.
- [ ] Навигация работает.
- [ ] Незавершённая партия восстанавливается после перезапуска.
- [ ] Undo работает в UI.
- [ ] `./gradlew assembleDebug` — BUILD SUCCESSFUL.

**Результат:** ⏳ Ожидает.

---

## Phase 5 — Полировка, release

**Цель:** довести до релизного качества.

**Модуль:** все.

**Зависит от:** Phase 4.

**Задачи:**

1. **Обработка ошибок:**
   - Пустое имя — inline.
   - Слишком длинное имя.
   - Дубликат.
   - Повреждённые данные — snackbar.
   - Тексты — человеческие.

2. **Пустые состояния:**
   - «История пуста».
   - «Нет незавершённой игры».
   - «Нет сыгранных партий».

3. **Анимации:**
   - Счёт — scale при изменении.
   - Серия — появление.
   - Переходы — 100–250ms.

4. **Иконка приложения:**
   - `ic_launcher` — свой.
   - Splash screen.

5. **Release:**
   - `versionCode`, `versionName`.
   - `proguard-rules.pro`.
   - Signing.
   - Сборка `assembleRelease`.

6. **README:**
   - Обновить.
   - Скриншоты.
   - Инструкция.

7. **Privacy policy** (если в Play Store).

8. **Документация:**
   - Дописать все `docs/*.md`.
   - Проверить актуальность.

9. **Локализация:**
   - Только русский в MVP.
   - Все строки — через `strings.xml`.
   - Никаких хардкод-строк в Compose.
   - Локализация на другие языки — v2+.

**Definition of Done:**

- [ ] `./gradlew assembleRelease` — BUILD SUCCESSFUL.
- [ ] APK подписан.
- [ ] Lint проходит без ошибок.
- [ ] Все тесты зелёные.
- [ ] Ручное тестирование: 3, 4, 8 игроков; portrait + landscape.
- [ ] README актуален.
- [ ] Готово к публикации (если нужно).

**Результат:** ⏳ Ожидает.

---

## Что НЕ входит ни в одну фазу (v2+)

- Backend, сервер, БД на сервере.
- Multiplayer, синхронизация.
- Авторизация, аккаунты.
- iOS, Web.
- Аналитика, crash reporting.
- Push-уведомления.
- Несколько режимов игры.
- Аватары, фото.
- Звуки.
- Реклама.

Всё это — **возможно** в будущем. Архитектура позволяет. Но 
**не в этом roadmap**.

---

## Правила работы с фазами

1. **Одна фаза за раз.** Не начинать Phase N+1 до завершения 
   Phase N.
2. **Definition of Done — обязателен.** Фаза не закрыта, пока все 
   пункты DoD не выполнены.
3. **Отклонения — фиксируются.** Если в ходе фазы принято решение 
   вне плана — записать в документ.
4. **Документы обновляются.** Если фаза меняет `DOMAIN_MODEL.md` 
   или `ARCHITECTURE.md` — обновить.
5. **Коммит в конце фазы.** Один коммит = закрытие фазы (или 
   несколько логических — но фаза закрывается явно).

---

## Текущий статус

**Сейчас:** Phase 1 (domain) — завершена (52 теста, все зелёные).

**Следующее:** Phase 2 (data) — Room, Repository.

**P.S.** UI-документы (`DESIGN_SYSTEM.md`, `UI_SPEC.md`, 
`SCREENS.md`, `TEST_PLAN.md`) создаются **после Phase 1**, перед 
Phase 3. Не раньше.