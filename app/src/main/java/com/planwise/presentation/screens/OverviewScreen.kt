package com.planwise.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planwise.R
import com.planwise.presentation.components.UiUtils
import com.planwise.presentation.viewmodel.OverviewViewModel

@Composable
fun OverviewScreen(
    onQuickAdd: () -> Unit,
    onOpenEvent: (String) -> Unit,
    vm: OverviewViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = stringResource(R.string.next_event), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val next = state.nextEvent
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (next == null) {
                    Text(stringResource(R.string.no_upcoming_events))
                } else {
                    Text(next.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(UiUtils.formatDateTime(next.startDateTime), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.today), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Button(onClick = onQuickAdd) { Text(stringResource(R.string.quick_add)) }
        }

        Spacer(Modifier.height(8.dp))
        if (state.todayEvents.isEmpty()) {
            Text(stringResource(R.string.no_events_today))
        } else {
            LazyColumn {
                items(state.todayEvents) { e ->
                    ListItem(
                        headlineContent = { Text(e.title) },
                        supportingContent = { Text(UiUtils.formatDateTime(e.startDateTime)) },
                        modifier = Modifier.clickable { onOpenEvent(e.id) }
                    )
                    Divider()
                }
            }
        }
    }
}
