package com.example.tiketiin.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class DetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val summary: EventSummary? = null,
    val listing: TicketListing? = null,
    val quantity: Int = 1,
    val selectedTab: Int = 0
) {
    val totalPrice: Long get() = (listing?.price ?: 0L) * quantity
}

class DetailViewModel(
    private val repository: TiketiinRepository,
    private val remote: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var viewRecorded = false

    fun load(eventId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val summary = if (remote) {
                    repository.getRemoteEventSummary(eventId)
                } else {
                    repository.getEventSummary(eventId)
                }

                if (summary == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = null,
                            listing = null,
                            errorMessage = "Event tidak ditemukan."
                        )
                    }
                    return@launch
                }

                val listing = if (remote) {
                    repository.getRemoteCheapestListing(eventId)
                } else {
                    repository.getCheapestListing(eventId)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                        listing = listing,
                        quantity = it.quantity.coerceIn(
                            1,
                            (listing?.quantity ?: 1).coerceAtLeast(1)
                        )
                    )
                }
                if (!viewRecorded) {
                    try {
                        if (remote) {
                            repository.recordRemoteView(eventId)
                        } else {
                            repository.recordView(eventId)
                        }
                        viewRecorded = true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        android.util.Log.w("DetailViewModel", "Gagal mencatat view event", e)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = null,
                        listing = null,
                        errorMessage = "Detail event gagal dimuat. Coba buka kembali."
                    )
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun changeQuantity(delta: Int) {
        _uiState.update {
            val max = (it.listing?.quantity ?: 1).coerceAtLeast(1)
            it.copy(quantity = (it.quantity + delta).coerceIn(1, max))
        }
    }

    fun toggleSaved() {
        val eventId = _uiState.value.summary?.event?.id ?: return
        viewModelScope.launch {
            repository.toggleSaved(eventId)
            load(eventId)
        }
    }
}
