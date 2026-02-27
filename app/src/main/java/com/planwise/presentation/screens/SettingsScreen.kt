package com.planwise.presentation.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.planwise.BuildConfig
import com.planwise.R
import com.planwise.data.settings.LanguageMode
import com.planwise.data.settings.ThemeMode
import com.planwise.presentation.viewmodel.SettingsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackMessage) {
        state.snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeSnack()
        }
    }

    val share: (android.net.Uri, String) -> Unit = { uri, mime ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importJson(uri)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.themeMode == ThemeMode.SYSTEM, onClick = { vm.setTheme(ThemeMode.SYSTEM) }, label = { Text(stringResource(R.string.theme_system)) })
                FilterChip(selected = state.themeMode == ThemeMode.LIGHT, onClick = { vm.setTheme(ThemeMode.LIGHT) }, label = { Text(stringResource(R.string.theme_light)) })
                FilterChip(selected = state.themeMode == ThemeMode.DARK, onClick = { vm.setTheme(ThemeMode.DARK) }, label = { Text(stringResource(R.string.theme_dark)) })
            }

            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.languageMode == LanguageMode.AUTO,
                    onClick = {
                        vm.setLanguage(LanguageMode.AUTO)
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    },
                    label = { Text(stringResource(R.string.lang_auto)) }
                )
                FilterChip(
                    selected = state.languageMode == LanguageMode.DE,
                    onClick = {
                        vm.setLanguage(LanguageMode.DE)
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("de"))
                    },
                    label = { Text(stringResource(R.string.lang_de)) }
                )
                FilterChip(
                    selected = state.languageMode == LanguageMode.EN,
                    onClick = {
                        vm.setLanguage(LanguageMode.EN)
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                    },
                    label = { Text(stringResource(R.string.lang_en)) }
                )
                FilterChip(
                    selected = state.languageMode == LanguageMode.RO,
                    onClick = {
                        vm.setLanguage(LanguageMode.RO)
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ro"))
                    },
                    label = { Text(stringResource(R.string.lang_ro)) }
                )
            }

            OutlinedTextField(
                value = state.defaultReminderMinutes.toString(),
                onValueChange = { vm.setDefaultReminder(it.toIntOrNull() ?: state.defaultReminderMinutes) },
                label = { Text(stringResource(R.string.default_reminder)) },
                singleLine = true
            )

            Divider()

            Text("Export/Import", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.exportJson { share(it, "application/json") } }) { Text(stringResource(R.string.export_json)) }
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) }) { Text(stringResource(R.string.import_json)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.exportIcs { share(it, "text/calendar") } }) { Text(stringResource(R.string.export_ics)) }
                OutlinedButton(onClick = {
                    val today = LocalDate.now()
                    vm.exportShiftPdf(today.year, today.monthValue) { share(it, "application/pdf") }
                }) { Text(stringResource(R.string.export_pdf_shifts)) }
            }

            Divider()

            Text(stringResource(R.string.cloud_sync), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(
                    checked = state.supabaseEnabled,
                    onCheckedChange = { vm.setSupabaseEnabled(it) }
                )
                Spacer(Modifier.width(8.dp))
                Text(if (state.supabaseEnabled) "ON" else "OFF")
            }

            OutlinedTextField(
                value = state.supabaseUrl.ifBlank { BuildConfig.PLANWISE_SUPABASE_URL },
                onValueChange = vm::setSupabaseUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.supabase_url)) },
                singleLine = true
            )
            OutlinedTextField(
                value = state.supabaseKey.ifBlank { BuildConfig.PLANWISE_SUPABASE_PUBLISHABLE_KEY },
                onValueChange = vm::setSupabaseKey,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.supabase_key)) },
                singleLine = true
            )

            if (state.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = { vm.syncNow() },
                enabled = state.supabaseEnabled && !state.isSyncing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sync_now))
            }
        }
    }
}
