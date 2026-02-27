package com.planwise.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planwise.R
import com.planwise.presentation.components.UiUtils
import com.planwise.presentation.viewmodel.EventDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    vm: EventDetailViewModel = hiltViewModel()
) {
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { vm.load(eventId) }
    val state by vm.uiState.collectAsState()
    val e = state.event

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            confirmButton = {
                TextButton(onClick = { vm.delete(eventId); confirmDelete = false; onBack() }) {
                    Text(stringResource(R.string.yes_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (e == null) {
            Text("—")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text(stringResource(R.string.cancel)) }
            return
        }

        Text(e.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("${stringResource(R.string.start)}: ${UiUtils.formatDateTime(e.startDateTime)}")
        e.endDateTime?.let { Text("${stringResource(R.string.end_optional)}: ${UiUtils.formatDateTime(it)}") }
        e.locationText?.let { Text("${stringResource(R.string.location_optional)}: $it") }
        e.description?.let { Text("${stringResource(R.string.description_optional)}: $it") }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onEdit) { Text(stringResource(R.string.edit)) }
            OutlinedButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.delete)) }
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.cancel)) }
        }
    }
}
