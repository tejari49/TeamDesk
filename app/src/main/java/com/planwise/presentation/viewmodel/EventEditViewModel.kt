package com.planwise.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planwise.data.settings.SettingsRepository
import com.planwise.domain.model.Event
import com.planwise.domain.model.RecurrenceType
import com.planwise.domain.repo.CategoryRepository
import com.planwise.domain.repo.EventRepository
import com.planwise.reminders.ReminderScheduler
import com.planwise.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EventEditUiState(
    val isLoading: Boolean = true,
    val eventId: String? = null,
    val title: String = "",
    val startDateTime: Long? = null,
    val endDateTime: Long? = null,
    val categoryId: String = "other",
    val subcategoryId: String? = null,
    val color: Int = 0xFF607D8B.toInt(),
    val locationText: String = "",
    val description: String = "",
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int = 1,
    val remindersMinutes: List<Int> = listOf(60),
    val validationError: String? = null,
)

@HiltViewModel
class EventEditViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val categoryRepository: CategoryRepository,
    private val settings: SettingsRepository,
    private val scheduler: ReminderScheduler,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventEditUiState())
    val uiState: StateFlow<EventEditUiState> = _uiState

    val categories = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subcategories = categoryRepository.observeSubcategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(eventId: String?) {
        viewModelScope.launch {
            val defaultReminder = settings.defaultReminderMinutes.first()
            if (eventId.isNullOrBlank()) {
                _uiState.value = EventEditUiState(
                    isLoading = false,
                    eventId = null,
                    startDateTime = System.currentTimeMillis() + 60_000,
                    remindersMinutes = listOf(defaultReminder),
                )
                return@launch
            }
            val event = eventRepository.getById(eventId)
            _uiState.value = if (event == null) {
                EventEditUiState(isLoading = false, eventId = null, remindersMinutes = listOf(defaultReminder))
            } else {
                EventEditUiState(
                    isLoading = false,
                    eventId = event.id,
                    title = event.title,
                    startDateTime = event.startDateTime,
                    endDateTime = event.endDateTime,
                    categoryId = event.categoryId,
                    subcategoryId = event.subcategoryId,
                    color = event.color,
                    locationText = event.locationText.orEmpty(),
                    description = event.description.orEmpty(),
                    recurrenceType = event.recurrenceType,
                    recurrenceInterval = event.recurrenceInterval,
                    remindersMinutes = event.remindersMinutes.ifEmpty { listOf(defaultReminder) }
                )
            }
        }
    }

    fun setTitle(v: String) = mutate { copy(title = v) }
    fun setStart(v: Long?) = mutate { copy(startDateTime = v) }
    fun setEnd(v: Long?) = mutate { copy(endDateTime = v) }
    fun setCategory(v: String) = mutate { copy(categoryId = v, subcategoryId = null) }
    fun setSubcategory(v: String?) = mutate { copy(subcategoryId = v) }
    fun setColor(v: Int) = mutate { copy(color = v) }
    fun setLocation(v: String) = mutate { copy(locationText = v) }
    fun setDescription(v: String) = mutate { copy(description = v) }
    fun setRecurrence(type: RecurrenceType) = mutate { copy(recurrenceType = type) }
    fun setRecurrenceInterval(i: Int) = mutate { copy(recurrenceInterval = i.coerceAtLeast(1)) }
    fun setReminders(list: List<Int>) = mutate { copy(remindersMinutes = list.distinct().sorted()) }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val st = _uiState.value
            val title = st.title.trim()
            val start = st.startDateTime
            if (title.isBlank() || start == null) {
                mutate { copy(validationError = "Titel und Start sind erforderlich") }
                return@launch
            }
            val now = System.currentTimeMillis()
            val id = st.eventId ?: UUID.randomUUID().toString()
            val createdAt = st.eventId?.let { eventRepository.getById(it)?.createdAt } ?: now
            val event = Event(
                id = id,
                title = title,
                startDateTime = start,
                endDateTime = st.endDateTime,
                categoryId = st.categoryId,
                subcategoryId = st.subcategoryId,
                color = st.color,
                locationText = st.locationText.trim().ifBlank { null },
                description = st.description.trim().ifBlank { null },
                recurrenceType = st.recurrenceType,
                recurrenceInterval = st.recurrenceInterval.coerceAtLeast(1),
                remindersMinutes = st.remindersMinutes.ifEmpty { listOf(settings.defaultReminderMinutes.first()) },
                createdAt = createdAt,
                updatedAt = now,
                deleted = false,
            )
            eventRepository.upsert(event)
            scheduler.cancelForEvent(event.id)
            scheduler.scheduleForEvent(event)
            widgetUpdater.requestUpdate()
            onSuccess()
        }
    }

    private inline fun mutate(block: EventEditUiState.() -> EventEditUiState) {
        _uiState.value = _uiState.value.block()
    }
}
