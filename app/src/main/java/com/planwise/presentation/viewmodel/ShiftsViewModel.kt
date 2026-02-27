package com.planwise.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planwise.domain.model.DayStatus
import com.planwise.domain.model.ShiftDay
import com.planwise.domain.model.ShiftType
import com.planwise.domain.repo.ShiftRepository
import com.planwise.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class ShiftViewMode { WEEK, MONTH }

data class ShiftsUiState(
    val mode: ShiftViewMode = ShiftViewMode.WEEK,
    val anchorDate: LocalDate = LocalDate.now(),
    val shiftMap: Map<Int, ShiftDay> = emptyMap(),
)

@HiltViewModel
class ShiftsViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    private val mode = MutableStateFlow(ShiftViewMode.WEEK)
    private val anchor = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<ShiftsUiState> = combine(
        shiftRepository.observeAll(),
        mode,
        anchor
    ) { shifts, m, a ->
        ShiftsUiState(
            mode = m,
            anchorDate = a,
            shiftMap = shifts.associateBy { it.dateYyyymmdd }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShiftsUiState())

    fun setMode(m: ShiftViewMode) { mode.value = m }
    fun setAnchor(d: LocalDate) { anchor.value = d }

    fun upsertDay(date: LocalDate, shiftType: ShiftType, status: DayStatus, note: String?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            shiftRepository.upsert(
                ShiftDay(
                    dateYyyymmdd = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth,
                    shiftType = shiftType,
                    dayStatus = status,
                    note = note?.trim().takeIf { !it.isNullOrBlank() },
                    updatedAt = now,
                )
            )
            widgetUpdater.requestUpdate()
        }
    }

    fun applyPattern(start: LocalDate, end: LocalDate, pattern: List<ShiftType>, defaultStatus: DayStatus) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            var idx = 0
            var d = start
            while (!d.isAfter(end)) {
                val st = pattern[idx % pattern.size]
                shiftRepository.upsert(
                    ShiftDay(
                        dateYyyymmdd = d.year * 10000 + d.monthValue * 100 + d.dayOfMonth,
                        shiftType = st,
                        dayStatus = if (st == ShiftType.NONE) DayStatus.FREE else defaultStatus,
                        note = null,
                        updatedAt = now,
                    )
                )
                idx++
                d = d.plusDays(1)
            }
            widgetUpdater.requestUpdate()
        }
    }
}
