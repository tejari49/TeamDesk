package com.planwise.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planwise.domain.model.Event
import com.planwise.domain.repo.EventRepository
import com.planwise.domain.usecase.ComputeNextOccurrenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class OverviewUiState(
    val nextEvent: Event? = null,
    val todayEvents: List<Event> = emptyList(),
)

@HiltViewModel
class OverviewViewModel @Inject constructor(
    eventRepository: EventRepository,
    private val nextOccurrence: ComputeNextOccurrenceUseCase,
) : ViewModel() {

    private val now = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<OverviewUiState> = combine(eventRepository.observeAll(), now) { events, nowMillis ->
        val enriched = events.filter { !it.deleted }.map { e ->
            val nextStart = nextOccurrence.nextStartAtOrAfter(e, nowMillis)
            e.copy(startDateTime = nextStart)
        }.sortedBy { it.startDateTime }

        val today = LocalDate.now()
        val todayEvents = enriched.filter { isSameDay(it.startDateTime, today) }.sortedBy { it.startDateTime }
        OverviewUiState(
            nextEvent = enriched.firstOrNull(),
            todayEvents = todayEvents
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverviewUiState())

    private fun isSameDay(epochMillis: Long, day: LocalDate): Boolean =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate() == day

    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                now.value = System.currentTimeMillis()
            }
        }
    }
}
