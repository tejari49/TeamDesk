package com.planwise.presentation.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planwise.R
import com.planwise.domain.model.RecurrenceType
import com.planwise.presentation.components.resolveStringKey
import com.planwise.presentation.viewmodel.EventEditViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: String?,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    vm: EventEditViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val state by vm.uiState.collectAsState()
    val categories by vm.categories.collectAsState()
    val subcategories by vm.subcategories.collectAsState()

    LaunchedEffect(eventId) { vm.load(eventId) }

    val snackHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.validationError) {
        state.validationError?.let { snackHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackHostState) }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = vm::setTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.title)) }
            )

            val startMillis = state.startDateTime ?: System.currentTimeMillis()
            val startZdt = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault())
            val startDate = startZdt.toLocalDate()
            val startTime = startZdt.toLocalTime()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = startMillis }
                        DatePickerDialog(ctx, { _, y, m, d ->
                            vm.setStart(startDateTimeMillis(LocalDate.of(y, m + 1, d), startTime))
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                ) { Text("${stringResource(R.string.start)}: $startDate") }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = startMillis }
                        TimePickerDialog(ctx, { _, hh, mm ->
                            vm.setStart(startDateTimeMillis(startDate, LocalTime.of(hh, mm)))
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }
                ) { Text("${startTime.hour.toString().padStart(2,'0')}:${startTime.minute.toString().padStart(2,'0')}") }
            }

            var hasEnd by remember(state.endDateTime) { mutableStateOf(state.endDateTime != null) }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = hasEnd, onCheckedChange = {
                    hasEnd = it
                    if (!it) vm.setEnd(null)
                    else vm.setEnd((state.startDateTime ?: System.currentTimeMillis()) + 60 * 60 * 1000)
                })
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.end_optional))
            }

            if (hasEnd) {
                val endMillis = state.endDateTime ?: (startMillis + 60 * 60 * 1000)
                val endZdt = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault())
                val endDate = endZdt.toLocalDate()
                val endTime = endZdt.toLocalTime()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = endMillis }
                            DatePickerDialog(ctx, { _, y, m, d ->
                                vm.setEnd(startDateTimeMillis(LocalDate.of(y, m + 1, d), endTime))
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    ) { Text("${stringResource(R.string.end_optional)}: $endDate") }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = endMillis }
                            TimePickerDialog(ctx, { _, hh, mm ->
                                vm.setEnd(startDateTimeMillis(endDate, LocalTime.of(hh, mm)))
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        }
                    ) { Text("${endTime.hour.toString().padStart(2,'0')}:${endTime.minute.toString().padStart(2,'0')}") }
                }
            }

            var catExpanded by remember { mutableStateOf(false) }
            val catLabel = categories.firstOrNull { it.id == state.categoryId }?.let { ctx.resolveStringKey(it.nameKey) } ?: state.categoryId
            ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
                OutlinedTextField(
                    value = catLabel,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text(stringResource(R.string.category)) }
                )
                ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(ctx.resolveStringKey(c.nameKey)) },
                            onClick = {
                                vm.setCategory(c.id)
                                vm.setColor(c.defaultColor)
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            val subsForCat = subcategories.filter { it.categoryId == state.categoryId }
            var subExpanded by remember { mutableStateOf(false) }
            val subLabel = subsForCat.firstOrNull { it.id == state.subcategoryId }?.let { ctx.resolveStringKey(it.nameKey) } ?: "—"
            ExposedDropdownMenuBox(expanded = subExpanded, onExpandedChange = { subExpanded = !subExpanded }) {
                OutlinedTextField(
                    value = subLabel,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text(stringResource(R.string.subcategory)) }
                )
                ExposedDropdownMenu(expanded = subExpanded, onDismissRequest = { subExpanded = false }) {
                    DropdownMenuItem(text = { Text("—") }, onClick = { vm.setSubcategory(null); subExpanded = false })
                    subsForCat.forEach { s ->
                        DropdownMenuItem(text = { Text(ctx.resolveStringKey(s.nameKey)) }, onClick = { vm.setSubcategory(s.id); subExpanded = false })
                    }
                }
            }

            Text(stringResource(R.string.color), style = MaterialTheme.typography.labelLarge)
            val palette = listOf(
                0xFF3F51B5.toInt(), 0xFF4CAF50.toInt(), 0xFFFF9800.toInt(),
                0xFF9C27B0.toInt(), 0xFF009688.toInt(), 0xFF607D8B.toInt(),
                0xFFF44336.toInt(), 0xFF795548.toInt()
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(palette) { c ->
                    val selected = c == state.color
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(c), shape = MaterialTheme.shapes.small)
                            .clickable { vm.setColor(c) }
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.locationText,
                onValueChange = vm::setLocation,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.location_optional)) }
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = vm::setDescription,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.description_optional)) }
            )

            var recExpanded by remember { mutableStateOf(false) }
            val recLabel = when (state.recurrenceType) {
                RecurrenceType.NONE -> stringResource(R.string.rec_none)
                RecurrenceType.DAILY -> stringResource(R.string.rec_daily)
                RecurrenceType.WEEKLY -> stringResource(R.string.rec_weekly)
                RecurrenceType.MONTHLY -> stringResource(R.string.rec_monthly)
            }
            ExposedDropdownMenuBox(expanded = recExpanded, onExpandedChange = { recExpanded = !recExpanded }) {
                OutlinedTextField(
                    value = recLabel,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text(stringResource(R.string.recurrence)) }
                )
                ExposedDropdownMenu(expanded = recExpanded, onDismissRequest = { recExpanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.rec_none)) }, onClick = { vm.setRecurrence(RecurrenceType.NONE); recExpanded = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.rec_daily)) }, onClick = { vm.setRecurrence(RecurrenceType.DAILY); recExpanded = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.rec_weekly)) }, onClick = { vm.setRecurrence(RecurrenceType.WEEKLY); recExpanded = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.rec_monthly)) }, onClick = { vm.setRecurrence(RecurrenceType.MONTHLY); recExpanded = false })
                }
            }

            Text(stringResource(R.string.reminders), style = MaterialTheme.typography.labelLarge)
            val presets = listOf(5, 15, 60, 1440)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { p ->
                    val selected = state.remindersMinutes.contains(p)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val next = if (selected) state.remindersMinutes - p else state.remindersMinutes + p
                            vm.setReminders(next)
                        },
                        label = { Text("${p}m") }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.save(onDone) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.save)) }
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
            }
        }
    }
}

private fun startDateTimeMillis(date: LocalDate, time: LocalTime): Long {
    val zdt = date.atTime(time).atZone(ZoneId.systemDefault())
    return zdt.toInstant().toEpochMilli()
}
