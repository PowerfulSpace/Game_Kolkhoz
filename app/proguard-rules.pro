# ProGuard/R8 rules for release build.
# Minification is disabled for now (isMinifyEnabled = false).

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepattributes *Annotation*

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Compose (обычно не требует правил, но на всякий случай)
-keep class androidx.compose.** { *; }

# Kotlin metadata
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Data classes (domain, data, ui)
-keep class ru.kolkhoz.domain.model.** { *; }
-keep class ru.kolkhoz.data.db.entity.** { *; }
-keep class ru.kolkhoz.ui.model.** { *; }

# Сохранить имена для отладки (убрать при релизе)
# -keepnames class ru.kolkhoz.** { *; }
