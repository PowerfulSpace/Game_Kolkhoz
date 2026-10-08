# TEST_PLAN.md — план тестирования «Колхоз»

Этот документ описывает **что** и **как** тестируем. Детали 
реализации тестов — в коде (`src/test/`, `src/androidTest/`).

---

## 1. Уровни тестирования

| Уровень | Модуль | Инструмент | Кол-во |
|---------|--------|------------|--------|
| Unit (domain) | `domain` | JUnit5 | 52 ✅ |
| Unit (mapper) | `data` | JUnit5 | 11 ✅ |
| Integration (Room) | `data` | JUnit4 + Robolectric | 24 ✅ |
| UI (Compose) | `app` | Compose UI Test | Phase 4 |
| Ручное | всё | на устройстве | Phase 5 |

**Итого сейчас:** 87 тестов, все зелёные.

---

## 2. Unit-тесты domain

**Модуль:** `domain/src/test/kotlin/`.

**Инструмент:** JUnit5.

**Что покрыто (52 теста):**

- `createGame`: 3/4/8 игроков; ошибки (<3, >8, пустые имена, 
  дубликаты, длинные имена, дубликаты `position`, выход за 
  диапазон).
- `recompute` / `applyShot`: одиночные `POCKET`, серии (2, 3, 5 
  подряд), `MISS`, `MISS` подряд, первый удар партии, 
  циклический порядок, отрицательный счёт.
- Пример из `SPEC.md` (раздел 4.5).
- `undoLastShot`: после `POCKET`, серии, `MISS`; пустая игра; 
  несколько undo; undo + повторный удар.
- `finishGame`: смена статуса, идемпотентность.
- `applyShot` на `FINISHED` — без изменений.
- `applyShot` с неверным `playerId` — `IllegalArgumentException`.
- Инвариант «сумма счетов = 0».

**Команда:** `./gradlew :domain:test`.

---

## 3. Unit-тесты мапперов (data)

**Модуль:** `data/src/test/kotlin/ru/kolkhoz/data/mapper/`.

**Инструмент:** JUnit5.

**Что покрыто (11 тестов):**

- Round-trip: `domain → entity → domain`.
- Маппинг enum (`ShotResult`, `GameStatus`).
- Неизвестное enum-значение → ошибка.
- `position` и `listOrder` не путаются.

**Команда:** `./gradlew :data:test` (включая mapper-тесты).

---

## 4. Integration-тесты Room (data)

**Модуль:** `data/src/test/kotlin/ru/kolkhoz/data/db/` и 
`repository/`.

**Инструмент:** JUnit4 + Robolectric.

**Что покрыто (24 теста):**

- `GameDao`: upsert, getById, observeActive, observeAll, delete.
- `PlayerDao`: `ORDER BY listOrder`, изоляция по gameId, CASCADE.
- `EventDao`: `ORDER BY sequenceNumber`, изоляция, CASCADE.
- `LocalGameRepository`: save/load/delete, observeActive, 
  observeAll, round-trip с перемешанными позициями.

**Команда:** `./gradlew :data:test`.

---

## 5. UI-тесты Compose (Phase 4)

**Модуль:** `app/src/androidTest/kotlin/`.

**Инструмент:** Compose UI Test.

**Что покрываем (после Phase 4):**

- **HomeScreen:** клик «НОВАЯ ИГРА» → переход.
- **NewGameScreen:** ввод имён → кнопка «НАЧАТЬ ИГРА» активна.
- **GameScreen:** клик «ЗАБИЛ» → счёт меняется; клик «ПРОМАХ» → 
  ход переходит.
- **Undo:** клик «ОТМЕНА УДАРА» → счёт возвращается.
- **ResultScreen:** сортировка по счёту.

**Не покрываем:** все крайние случаи (это делают unit-тесты).

**Команда:** `./gradlew :app:connectedAndroidTest` (нужен 
эмулятор/устройство).

---

## 6. Ручное тестирование (Phase 5)

**Устройство:** реальный Android-смартфон (minSdk 26).

**Что проверяем:**

- **Создание партии** — 3, 4, 8 игроков.
- **Полная партия** — от создания до завершения.
- **Серии** — забить подряд 2, 3, 5 раз.
- **Undo + Redo** — отмена и возврат.
- **История** — все удары корректны.
- **Portrait / landscape** — игровой экран и история.
- **Перезапуск** — незавершённая партия восстанавливается.
- **Оффлайн** — работает без интернета.
- **Отрицательный счёт** — игрок уходит в минус.
- **Пустые состояния** — «нет истории», «нет активной партии».
- **Ошибки** — пустое имя, дубликат, длинное имя.

---

## 7. DoD (Definition of Done)

Перед релизом:

- [ ] `./gradlew :domain:test` — BUILD SUCCESSFUL (52).
- [ ] `./gradlew :data:test` — BUILD SUCCESSFUL (35).
- [ ] `./gradlew :app:connectedAndroidTest` — BUILD SUCCESSFUL 
      (после Phase 4).
- [ ] Ручное тестирование пройдено.
- [ ] Lint проходит без ошибок.
- [ ] `./gradlew assembleRelease` — BUILD SUCCESSFUL.

---

## 8. Ссылки

- `DOMAIN_MODEL.md` — что тестируем в domain.
- `ARCHITECTURE.md` — где живут тесты.
- `ROADMAP.md` — фазы.