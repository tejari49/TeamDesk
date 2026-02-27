package com.planwise.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "planwise_settings")

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANG = stringPreferencesKey("language")
        val DEFAULT_REMINDER = intPreferencesKey("default_reminder")

        val SB_ENABLED = booleanPreferencesKey("sb_enabled")
        val SB_URL = stringPreferencesKey("sb_url")
        val SB_KEY = stringPreferencesKey("sb_key")

        val SB_ACCESS = stringPreferencesKey("sb_access")
        val SB_REFRESH = stringPreferencesKey("sb_refresh")
        val SB_EXPIRES_IN = longPreferencesKey("sb_expires_in")
        val SB_TOKEN_TYPE = stringPreferencesKey("sb_token_type")
    }

    override val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map { pref ->
            when (pref[Keys.THEME]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    override val languageMode: Flow<LanguageMode> =
        context.dataStore.data.map { pref ->
            when (pref[Keys.LANG]) {
                LanguageMode.DE.name -> LanguageMode.DE
                LanguageMode.EN.name -> LanguageMode.EN
                LanguageMode.RO.name -> LanguageMode.RO
                else -> LanguageMode.AUTO
            }
        }

    override val defaultReminderMinutes: Flow<Int> =
        context.dataStore.data.map { it[Keys.DEFAULT_REMINDER] ?: 60 }

    override val supabaseConfig: Flow<SupabaseConfig> =
        context.dataStore.data.map { pref ->
            SupabaseConfig(
                enabled = pref[Keys.SB_ENABLED] ?: false,
                url = pref[Keys.SB_URL] ?: "",
                key = pref[Keys.SB_KEY] ?: "",
            )
        }

    override val supabaseSession: Flow<SupabaseSession?> =
        context.dataStore.data.map { pref ->
            val access = pref[Keys.SB_ACCESS] ?: return@map null
            val refresh = pref[Keys.SB_REFRESH] ?: return@map null
            val expiresIn = pref[Keys.SB_EXPIRES_IN] ?: return@map null
            val tokenType = pref[Keys.SB_TOKEN_TYPE] ?: "Bearer"
            SupabaseSession(access, refresh, expiresIn, tokenType)
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    override suspend fun setLanguageMode(mode: LanguageMode) {
        context.dataStore.edit { it[Keys.LANG] = mode.name }
    }

    override suspend fun setDefaultReminderMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_REMINDER] = minutes.coerceAtLeast(0) }
    }

    override suspend fun setSupabaseEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SB_ENABLED] = enabled }
    }

    override suspend fun setSupabaseUrl(url: String) {
        context.dataStore.edit { it[Keys.SB_URL] = url.trim() }
    }

    override suspend fun setSupabaseKey(key: String) {
        context.dataStore.edit { it[Keys.SB_KEY] = key.trim() }
    }

    override suspend fun setSupabaseSession(session: SupabaseSession?) {
        context.dataStore.edit { pref ->
            if (session == null) {
                pref.remove(Keys.SB_ACCESS)
                pref.remove(Keys.SB_REFRESH)
                pref.remove(Keys.SB_EXPIRES_IN)
                pref.remove(Keys.SB_TOKEN_TYPE)
            } else {
                pref[Keys.SB_ACCESS] = session.accessToken
                pref[Keys.SB_REFRESH] = session.refreshToken
                pref[Keys.SB_EXPIRES_IN] = session.expiresIn
                pref[Keys.SB_TOKEN_TYPE] = session.tokenType
            }
        }
    }
}
