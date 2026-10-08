package ru.kolkhoz

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Точка входа Hilt. Обязательна для работы @AndroidEntryPoint в MainActivity.
 */
@HiltAndroidApp
class KolkhozApplication : Application()
