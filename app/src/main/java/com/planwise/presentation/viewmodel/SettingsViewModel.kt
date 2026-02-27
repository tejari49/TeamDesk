package com.planwise.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planwise.data.export.ExportImportManager
import com.planwise.data.export.IcsExporter
import com.planwise.data.export.ShiftPdfExporter
import com.planwise.data.settings.LanguageMode
import com.planwise.data.settings.SettingsRepository
import com.planwise.data.settings.ThemeMode
import com.planwise.sync.CloudSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.AUTO,
    val defaultReminderMinutes: Int = 60,
    val supabaseEnabled: Boolean = false,
    val supabaseUrl: String = "",
    val supabaseKey: String = "",
    val isSyncing: Boolean = false,
    val snackMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val exportImport: ExportImportManager,
    private val icsExporter: IcsExporter,
    private val pdfExporter: ShiftPdfExporter,
    private val syncManager: CloudSyncManager,
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _snack = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settings.themeMode,
        settings.languageMode,
        settings.defaultReminderMinutes,
        settings.supabaseConfig,
        _isSyncing,
        _snack
    ) { theme, lang, defRem, sb, syncing, snack ->
        SettingsUiState(
            themeMode = theme,
            languageMode = lang,
            defaultReminderMinutes = defRem,
            supabaseEnabled = sb.enabled,
            supabaseUrl = sb.url,
            supabaseKey = sb.key,
            isSyncing = syncing,
            snackMessage = snack,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun consumeSnack() { _snack.value = null }

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setLanguage(mode: LanguageMode) = viewModelScope.launch { settings.setLanguageMode(mode) }
    fun setDefaultReminder(minutes: Int) = viewModelScope.launch { settings.setDefaultReminderMinutes(minutes) }

    fun setSupabaseEnabled(enabled: Boolean) = viewModelScope.launch { settings.setSupabaseEnabled(enabled) }
    fun setSupabaseUrl(url: String) = viewModelScope.launch { settings.setSupabaseUrl(url) }
    fun setSupabaseKey(key: String) = viewModelScope.launch { settings.setSupabaseKey(key) }

    fun exportJson(onReady: (android.net.Uri) -> Unit) {
        viewModelScope.launch {
            val uri = exportImport.exportToCacheFile()
            onReady(uri)
        }
    }

    fun exportIcs(onReady: (android.net.Uri) -> Unit) {
        viewModelScope.launch {
            val uri = icsExporter.exportIcsToCache()
            onReady(uri)
        }
    }

    fun exportShiftPdf(year: Int, month: Int, onReady: (android.net.Uri) -> Unit) {
        viewModelScope.launch {
            val uri = pdfExporter.exportMonth(year, month)
            onReady(uri)
        }
    }

    fun importJson(uri: android.net.Uri) {
        viewModelScope.launch {
            val res = exportImport.importFromUri(uri)
            _snack.value = if (res.isSuccess) "Import OK" else "Import Fehler: ${res.exceptionOrNull()?.message}"
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = syncManager.syncNow()
            _snack.value = if (res.isSuccess) "Sync erfolgreich ✅" else "Sync fehlgeschlagen: ${res.exceptionOrNull()?.message}"
            _isSyncing.value = false
        }
    }
}
