package com.example.tiketiin.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedUiState(
    val isLoading: Boolean = false,
    val items: List<EventSummary> = emptyList(),
    val selectedCategory: EventCategory? = null,
    // Satu transaksi untuk satu event, jadi hanya satu yang bisa dicentang.
    val selectedEventId: Int? = null
) {
    val visibleItems: List<EventSummary>
        get() = if (selectedCategory == null) items else items.filter { it.event.category == selectedCategory }

    val total: Long
        get() = items.find { it.event.id == selectedEventId }?.lowestPrice ?: 0L
}

class SavedViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.items.isEmpty()) }
            val saved = repository.getEventSummaries().filter { it.isSaved }
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    items = saved,
                    selectedEventId = state.selectedEventId?.takeIf { id -> saved.any { it.event.id == id } }
                )
            }
        }
    }

    fun selectCategory(category: EventCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleSelected(eventId: Int) {
        _uiState.update {
            it.copy(selectedEventId = if (it.selectedEventId == eventId) null else eventId)
        }
    }

    fun remove(eventId: Int) {
        viewModelScope.launch {
            repository.toggleSaved(eventId)
            load()
        }
    }
}
