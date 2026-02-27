package com.planwise.data.settings

import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class LanguageMode { AUTO, DE, EN, RO }

data class SupabaseConfig(
    val enabled: Boolean,
    val url: String,
    val key: String,
)

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
)

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val languageMode: Flow<LanguageMode>
    val defaultReminderMinutes: Flow<Int>
    val supabaseConfig: Flow<SupabaseConfig>
    val supabaseSession: Flow<SupabaseSession?>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguageMode(mode: LanguageMode)
    suspend fun setDefaultReminderMinutes(minutes: Int)

    suspend fun setSupabaseEnabled(enabled: Boolean)
    suspend fun setSupabaseUrl(url: String)
    suspend fun setSupabaseKey(key: String)
    suspend fun setSupabaseSession(session: SupabaseSession?)
}
