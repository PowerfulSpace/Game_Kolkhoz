# Колхоз

Android-приложение для подсчёта очков в бильярде.

## Что это

«Колхоз» — счётчик очков для игры в бильярд: 3–8 игроков,
забитые шары и промахи, серии, отмена удара и история партии.
Минимум интерфейса — приложение нажимается между ударами,
не требует чтения инструкций.

## Скриншоты

Скриншоты будут добавлены после первой сборки.

## Стек

- Kotlin
- Jetpack Compose
- Hilt
- Room
- Coroutines

## Структура

- `app/` — UI, навигация, ViewModel
- `domain/` — бизнес-логика (правила игры)
- `data/` — Room, репозитории
- `docs/` — документация

## Сборка

```bash
./gradlew assembleDebug
```

(требуется Android SDK)

## Сборка release

```bash
./gradlew assembleRelease
```

Требуется настроенный signing (keystore) в
`app/build.gradle.kts`. Настраивается в Phase 5b.

## Статус

- Phase 0–5a: ✅ завершены.
- Phase 5b (release): ⏳ ожидает проверки сборки
  в Android Studio.

## Документация

- docs/SPEC.md — спецификация продукта
- docs/DOMAIN_MODEL.md — правила игры
- docs/ARCHITECTURE.md — архитектура
- docs/DESIGN_SYSTEM.md — дизайн-система
- docs/UI_SPEC.md — экраны

## Лицензия

См. LICENSE.
