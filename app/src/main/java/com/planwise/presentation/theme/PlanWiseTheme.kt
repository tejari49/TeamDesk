package com.planwise.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.planwise.data.settings.SettingsRepository
import com.planwise.data.settings.ThemeMode
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeEntryPoint {
    fun settingsRepository(): SettingsRepository
}

@Composable
fun PlanWiseTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val ep = EntryPointAccessors.fromApplication(context, ThemeEntryPoint::class.java)
    val settings = ep.settingsRepository()
    val mode by settings.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    val isDark = when (mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        content = content
    )
}
