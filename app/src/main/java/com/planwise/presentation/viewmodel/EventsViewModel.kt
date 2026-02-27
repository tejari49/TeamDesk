package com.planwise.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planwise.domain.model.Category
import com.planwise.domain.model.Event
import com.planwise.domain.model.Subcategory
import com.planwise.domain.repo.CategoryRepository
import com.planwise.domain.repo.EventRepository
import com.planwise.domain.usecase.ComputeNextOccurrenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class EventsDateFilter { UPCOMING, TODAY, WEEK, ALL }

data class EventsUiState(
    val query: String = "",
    val dateFilter: EventsDateFilter = EventsDateFilter.UPCOMING,
    val categoryId: String? = null,
    val events: List<Event> = emptyList(),
    val categories: List<Category> = emptyList(),
    val subcategories: List<Subcategory> = emptyList(),
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    eventRepository: EventRepository,
    categoryRepository: CategoryRepository,
    private val nextOccurrence: ComputeNextOccurrenceUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val dateFilter = MutableStateFlow(EventsDateFilter.UPCOMING)
    private val categoryId = MutableStateFlow<String?>(null)

    fun setQuery(q: String) { query.value = q }
    fun setDateFilter(f: EventsDateFilter) { dateFilter.value = f }
    fun setCategory(cat: String?) { categoryId.value = cat }

    val uiState: StateFlow<EventsUiState> = combine(
        eventRepository.observeAll(),
        categoryRepository.observeCategories(),
        categoryRepository.observeSubcategories(),
        query, dateFilter, categoryId
    ) { events, cats, subs, q, df, cat ->
        val now = System.currentTimeMillis()
        val enriched = events.filter { !it.deleted }.map { e ->
            val nextStart = nextOccurrence.nextStartAtOrAfter(e, now)
            e.copy(startDateTime = nextStart)
        }

        val filtered = enriched
            .filter { matchesQuery(it, q) }
            .filter { cat == null || it.categoryId == cat }
            .filter { matchesDate(it, df, now) }
            .sortedBy { it.startDateTime }

        EventsUiState(
            query = q,
            dateFilter = df,
            categoryId = cat,
            events = filtered,
            categories = cats,
            subcategories = subs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventsUiState())

    private fun matchesQuery(e: Event, q: String): Boolean {
        val t = q.trim()
        if (t.isEmpty()) return true
        return e.title.contains(t, ignoreCase = true) || (e.description?.contains(t, ignoreCase = true) == true)
    }

    private fun matchesDate(e: Event, df: EventsDateFilter, nowMillis: Long): Boolean {
        val date = Instant.ofEpochMilli(e.startDateTime).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        return when (df) {
            EventsDateFilter.ALL -> true
            EventsDateFilter.TODAY -> date == today
            EventsDateFilter.WEEK -> !date.isBefore(today) && date.isBefore(today.plusDays(7))
            EventsDateFilter.UPCOMING -> e.startDateTime >= nowMillis
        }
    }
}
