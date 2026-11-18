package com.example.tiketiin.ui.home

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
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val banner: EventSummary? = null,
    val flashTickets: List<EventSummary> = emptyList(),
    val popularEvents: List<EventSummary> = emptyList(),
    val selectedCategory: EventCategory? = null
) {
    // Event Populer setelah disaring chip kategori.
    val filteredPopular: List<EventSummary>
        get() = if (selectedCategory == null) {
            popularEvents
        } else {
            popularEvents.filter { it.event.category == selectedCategory }
        }
}

class HomeViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.popularEvents.isEmpty(), errorMessage = null) }
            try {
                val today = LocalDate.now()
                val active = repository.getEventSummaries()
                    .filter { !it.event.date.isBefore(today) && it.ticketCount > 0 }
                val popular = active.sortedByDescending { it.event.viewCount }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        banner = popular.firstOrNull(),
                        flashTickets = active
                            .filter { it.event.flashEndsInHours != null }
                            .sortedBy { it.event.flashEndsInHours },
                        popularEvents = popular
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat event")
                }
            }
        }
    }

    fun selectCategory(category: EventCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
