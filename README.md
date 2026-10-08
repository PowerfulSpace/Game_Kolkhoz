# Game_Kolkhoz — «Колхоз»

Android-приложение для подсчёта очков в бильярде.

## Описание

Многомодульный Android-проект на Kotlin и Jetpack Compose. На текущем шаге
подготовлен только каркас проекта: модули, сборочные скрипты, DI (Hilt) и
хранилище (Room) подключены, бизнес-логики и экранов пока нет — приложение
показывает пустой экран с названием «Колхоз».

## Стек

- Kotlin 2.4.21 (JVM target 17)
- Jetpack Compose (BOM 2026.09.00), Material 3
- Hilt (DI) — модули `app` и `data`
- Room + KSP — модуль `data`
- Kotlin Coroutines + Flow
- JUnit 5 (JUnit Platform) — тесты модуля `domain`
- AndroidX: core-ktx, lifecycle, activity-compose, navigation-compose
- Gradle 9.8.1 + Version Catalog (`gradle/libs.versions.toml`)
- Android Gradle Plugin 9.4.1, minSdk 26, compileSdk/targetSdk 37, Java 17

## Структура модулей

```
Game_Kolkhoz/
├── app/      — Android-приложение: UI (Compose), навигация, DI-обвязка
├── domain/   — чистый Kotlin/JVM-модуль, без Android-зависимостей
├── data/     — Android-библиотека: Room, репозитории
├── docs/     — документация
└── gradle/   — Version Catalog (libs.versions.toml) и Gradle Wrapper
```

## Сборка

```bash
./gradlew assembleDebug
```

Тесты:

```bash
./gradlew test
```
