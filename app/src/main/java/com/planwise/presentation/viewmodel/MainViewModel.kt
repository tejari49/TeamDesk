package com.planwise.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planwise.data.seed.SeedInitializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(val seeded: Boolean = false)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val seedInitializer: SeedInitializer
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        viewModelScope.launch {
            seedInitializer.ensureSeeded()
            _uiState.value = MainUiState(seeded = true)
        }
    }
}
