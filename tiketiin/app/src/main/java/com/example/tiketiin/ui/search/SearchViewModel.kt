package com.example.tiketiin.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import com.example.tiketiin.data.repository.EventCatalogRepository

data class SearchUiState(
    val query: String = "",
    val category: EventCategory? = null,
    val sortByCheapest: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val results: List<EventSummary> = emptyList()
)

class SearchViewModel(
    private val repository: TiketiinRepository,
    initialCategory: EventCategory?,
    private val catalog: EventCatalogRepository,
    private val flashOnly: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(category = initialCategory))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        search(withDelay = false)
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        search(withDelay = true)
    }

    fun selectCategory(category: EventCategory?) {
        _uiState.update { it.copy(category = category) }
        search(withDelay = false)
    }

    fun toggleSort() {
        _uiState.update { it.copy(sortByCheapest = !it.sortByCheapest) }
        search(withDelay = false)
    }

    fun toggleSaved(eventId: Int) {
        viewModelScope.launch {
            repository.toggleSaved(eventId)
            search(withDelay = false)
        }
    }

    fun refresh() = search(withDelay = false)

    // Jeda 400 ms saat mengetik supaya tidak mencari di setiap huruf (debounce).
    private fun search(withDelay: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (withDelay) delay(400)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val today = LocalDate.now()
                // Event yang sudah lewat atau tiketnya habis tidak ditampilkan.
                val cached = catalog.getCachedEvents()
                val events = if (cached.isEmpty()) {
                    catalog.refreshEvents()
                    catalog.getCachedEvents()
                } else {
                    cached
                }

                val query = state.query.trim()
                val found = events.filter { item ->
                    val event = item.event
                    val matchesQuery = query.isBlank() ||
                            event.name.contains(query, ignoreCase = true) ||
                            event.venue.contains(query, ignoreCase = true) ||
                            event.city.contains(query, ignoreCase = true)

                    matchesQuery &&
                            (state.category == null || event.category == state.category) &&
                            !event.date.isBefore(today) &&
                            item.ticketCount > 0 &&
                            (!flashOnly || (event.flashEndsInHours ?: 0) > 0)
                }
                val sorted = if (state.sortByCheapest) {
                    found.sortedBy { it.lowestPrice }
                } else {
                    found.sortedBy { it.event.date }
                }
                _uiState.update { it.copy(isLoading = false, results = sorted) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal mencari event")
                }
            }
        }
    }
}
