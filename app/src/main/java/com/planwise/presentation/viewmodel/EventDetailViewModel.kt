package com.planwise.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planwise.domain.model.Event
import com.planwise.domain.repo.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailUiState(val event: Event? = null)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState

    fun load(id: String) {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState(eventRepository.getById(id))
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            eventRepository.softDelete(id)
            _uiState.value = EventDetailUiState(null)
        }
    }
}
