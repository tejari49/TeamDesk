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
import com.planwise.domain.model.DayStatus
import com.planwise.domain.model.ShiftType
import com.planwise.presentation.viewmodel.ShiftViewMode
import com.planwise.presentation.viewmodel.ShiftsViewModel
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen(
    vm: ShiftsViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    var editorDate by remember { mutableStateOf<LocalDate?>(null) }
    var showGenerator by remember { mutableStateOf(false) }

    if (editorDate != null) {
        ShiftEditBottomSheet(
            date = editorDate!!,
            existing = state.shiftMap[editorDate!!.toYyyymmdd()],
            onDismiss = { editorDate = null },
            onSave = { shift, status, note ->
                vm.upsertDay(editorDate!!, shift, status, note)
                editorDate = null
            }
        )
    }

    if (showGenerator) {
        ShiftGeneratorBottomSheet(
            anchor = state.anchorDate,
            onDismiss = { showGenerator = false },
            onApply = { start, end, pattern ->
                vm.applyPattern(start, end, pattern, DayStatus.NORMAL)
                showGenerator = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = state.mode == ShiftViewMode.WEEK,
                onClick = { vm.setMode(ShiftViewMode.WEEK) },
                label = { Text(stringResource(R.string.week_view)) }
            )
            FilterChip(
                selected = state.mode == ShiftViewMode.MONTH,
                onClick = { vm.setMode(ShiftViewMode.MONTH) },
                label = { Text(stringResource(R.string.month_view)) }
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = { showGenerator = true }) { Text(stringResource(R.string.generator)) }
        }

        Spacer(Modifier.height(8.dp))

        if (state.mode == ShiftViewMode.WEEK) {
            WeekView(anchor = state.anchorDate, shiftMap = state.shiftMap, onDayClick = { editorDate = it })
        } else {
            MonthView(anchor = state.anchorDate, shiftMap = state.shiftMap, onDayClick = { editorDate = it })
        }
    }
}

@Composable
private fun WeekView(anchor: LocalDate, shiftMap: Map<Int, com.planwise.domain.model.ShiftDay>, onDayClick: (LocalDate) -> Unit) {
    val start = anchor.minusDays(((anchor.dayOfWeek.value + 6) % 7).toLong())
    val days = (0..6).map { start.plusDays(it.toLong()) }
    LazyColumn {
        items(days) { d ->
            val sd = shiftMap[d.toYyyymmdd()]
            ListItem(
                headlineContent = { Text(d.toString()) },
                supportingContent = { Text(formatShift(sd)) },
                modifier = Modifier.clickable { onDayClick(d) }
            )
            Divider()
        }
    }
}

@Composable
private fun MonthView(anchor: LocalDate, shiftMap: Map<Int, com.planwise.domain.model.ShiftDay>, onDayClick: (LocalDate) -> Unit) {
    val ym = YearMonth.of(anchor.year, anchor.monthValue)
    val first = ym.atDay(1)
    val days = (0 until ym.lengthOfMonth()).map { first.plusDays(it.toLong()) }
    LazyColumn {
        items(days) { d ->
            val sd = shiftMap[d.toYyyymmdd()]
            ListItem(
                headlineContent = { Text(d.toString()) },
                supportingContent = { Text(formatShift(sd)) },
                modifier = Modifier.clickable { onDayClick(d) }
            )
            Divider()
        }
    }
}

private fun formatShift(sd: com.planwise.domain.model.ShiftDay?): String {
    if (sd == null) return "—"
    return "${sd.shiftType} / ${sd.dayStatus}" + (sd.note?.let { " • $it" } ?: "")
}

private fun LocalDate.toYyyymmdd(): Int = year * 10000 + monthValue * 100 + dayOfMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftEditBottomSheet(
    date: LocalDate,
    existing: com.planwise.domain.model.ShiftDay?,
    onDismiss: () -> Unit,
    onSave: (ShiftType, DayStatus, String?) -> Unit
) {
    var shift by remember { mutableStateOf(existing?.shiftType ?: ShiftType.NONE) }
    var status by remember { mutableStateOf(existing?.dayStatus ?: DayStatus.NORMAL) }
    var note by remember { mutableStateOf(existing?.note.orEmpty()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = date.toString(), style = MaterialTheme.typography.titleMedium)

            Text(stringResource(R.string.shift_type))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ShiftType.EARLY, ShiftType.LATE, ShiftType.NIGHT, ShiftType.NONE).forEach { st ->
                    FilterChip(selected = shift == st, onClick = { shift = st }, label = { Text(st.name) })
                }
            }

            Text(stringResource(R.string.day_status))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(DayStatus.NORMAL, DayStatus.FREE, DayStatus.VACATION, DayStatus.SICK).forEach { ds ->
                    FilterChip(selected = status == ds, onClick = { status = ds }, label = { Text(ds.name) })
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.note_optional)) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onSave(shift, status, note.ifBlank { null }) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.save)) }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftGeneratorBottomSheet(
    anchor: LocalDate,
    onDismiss: () -> Unit,
    onApply: (LocalDate, LocalDate, List<ShiftType>) -> Unit
) {
    var start by remember { mutableStateOf(anchor) }
    var end by remember { mutableStateOf(anchor.plusDays(13)) }
    var pattern by remember { mutableStateOf(listOf(ShiftType.EARLY, ShiftType.LATE, ShiftType.NIGHT, ShiftType.NONE)) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.generator), style = MaterialTheme.typography.titleMedium)
            Text("Start: $start")
            Text("End: $end")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { start = start.minusDays(1) }) { Text("Start -1") }
                OutlinedButton(onClick = { start = start.plusDays(1) }) { Text("Start +1") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { end = end.minusDays(1) }) { Text("End -1") }
                OutlinedButton(onClick = { end = end.plusDays(1) }) { Text("End +1") }
            }

            Text("Pattern: " + pattern.joinToString("→") { it.name })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pattern = listOf(ShiftType.EARLY, ShiftType.LATE, ShiftType.NIGHT, ShiftType.NONE) }) { Text("E→L→N→OFF") }
                OutlinedButton(onClick = { pattern = listOf(ShiftType.EARLY, ShiftType.EARLY, ShiftType.LATE, ShiftType.NIGHT, ShiftType.NONE) }) { Text("2E→L→N→OFF") }
            }

            Button(onClick = { onApply(start, end, pattern) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.apply_pattern))
            }
        }
    }
}
