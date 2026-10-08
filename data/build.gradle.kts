import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// AGP 9.0+ содержит встроенную поддержку Kotlin, плагин
// org.jetbrains.kotlin.android здесь больше не применяется.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ru.kolkhoz.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            // Robolectric нуждается в ресурсах Android в unit-тестах.
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Разделение тестов (AGENTS.md, раздел 6.4):
    //  - мапперы и чистая логика — JUnit5 (org.junit.jupiter.api.Test);
    //  - Room DAO и LocalGameRepository — JUnit4 + Robolectric
    //    (@RunWith(RobolectricTestRunner::class)), запускаются vintage-движком.
    // Оба движка сосуществуют в одном ./gradlew :data:test.
    testImplementation(libs.junit.vintage)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    // Robolectric (ApplicationSharedMemory.create) обращается к
    // jdk.internal.access.SharedSecrets: модульная система Java 21 не отдаёт
    // этот пакет unnamed module без явной отдачи, из-за чего все Room-тесты
    // падают с IllegalAccessException.
    jvmArgs("--add-exports", "java.base/jdk.internal.access=ALL-UNNAMED")
}
