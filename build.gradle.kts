// Корневой файл: только подключение плагинов, сама конфигурация — в модулях.
//
// kotlin.android здесь объявлен с `apply false`: в Android-модулях его больше
// нельзя применять (AGP 9+ сам подключает Kotlin), но объявление гарантирует,
// что в classpath сборки попадает Kotlin 2.4.21 — последняя стабильная версия.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
