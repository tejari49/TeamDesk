package com.planwise.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planwise.R
import com.planwise.presentation.components.UiUtils
import com.planwise.presentation.components.resolveCategoryName
import com.planwise.presentation.components.resolveStringKey
import com.planwise.presentation.viewmodel.EventsDateFilter
import com.planwise.presentation.viewmodel.EventsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onCreate: () -> Unit,
    onOpenEvent: (String) -> Unit,
    vm: EventsViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::setQuery,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search)) },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.dateFilter == EventsDateFilter.UPCOMING,
                onClick = { vm.setDateFilter(EventsDateFilter.UPCOMING) },
                label = { Text(stringResource(R.string.filter_upcoming)) }
            )
            FilterChip(
                selected = state.dateFilter == EventsDateFilter.TODAY,
                onClick = { vm.setDateFilter(EventsDateFilter.TODAY) },
                label = { Text(stringResource(R.string.filter_today)) }
            )
            FilterChip(
                selected = state.dateFilter == EventsDateFilter.WEEK,
                onClick = { vm.setDateFilter(EventsDateFilter.WEEK) },
                label = { Text(stringResource(R.string.filter_week)) }
            )
            FilterChip(
                selected = state.dateFilter == EventsDateFilter.ALL,
                onClick = { vm.setDateFilter(EventsDateFilter.ALL) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
        }

        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        val catLabel = state.categoryId?.let { resolveCategoryName(context, state.categories, it) } ?: "—"
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = catLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                label = { Text(stringResource(R.string.category)) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("—") },
                    onClick = { vm.setCategory(null); expanded = false }
                )
                state.categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(context.resolveStringKey(c.nameKey)) },
                        onClick = { vm.setCategory(c.id); expanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FloatingActionButton(onClick = onCreate) {
                Icon(androidx.compose.material.icons.Icons.Outlined.Add, contentDescription = stringResource(R.string.add))
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.events) { e ->
                ListItem(
                    headlineContent = { Text(e.title) },
                    supportingContent = {
                        Text("${UiUtils.formatDateTime(e.startDateTime)} • ${resolveCategoryName(context, state.categories, e.categoryId)}")
                    },
                    modifier = Modifier.clickable { onOpenEvent(e.id) }
                )
                Divider()
            }
        }
    }
}
