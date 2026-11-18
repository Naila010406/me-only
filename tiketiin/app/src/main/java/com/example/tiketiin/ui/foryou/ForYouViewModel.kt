package com.example.tiketiin.ui.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.repository.TiketiinRepository
import com.example.tiketiin.domain.RecommendationCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

data class ForYouUiState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,                 // 0 = Rekomendasi Untukmu, 1 = Di Sekitarmu
    val selectedCategory: EventCategory? = null,
    val recommended: List<EventSummary> = emptyList(),
    val nearby: List<EventSummary> = emptyList()
) {
    val visibleEvents: List<EventSummary>
        get() {
            val source = if (selectedTab == 0) recommended else nearby
            return if (selectedCategory == null) source else source.filter { it.event.category == selectedCategory }
        }
}

class ForYouViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForYouUiState())
    val uiState: StateFlow<ForYouUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.recommended.isEmpty(), errorMessage = null) }
            try {
                val today = LocalDate.now()
                val user = repository.getUser()
                val all = repository.getEventSummaries()
                val byId = all.associateBy { it.event.id }

                // Profil preferensi: kategori dari riwayat order dan event tersimpan.
                val orders = repository.getMyOrders()
                val purchasedIds = orders.map { it.eventId }.toSet()
                val purchasedCategories = orders.mapNotNull { byId[it.eventId]?.event?.category }
                val savedCategories = all.filter { it.isSaved }.map { it.event.category }

                val recommended = RecommendationCalculator.recommend(
                    candidates = all,
                    purchasedCategories = purchasedCategories,
                    savedCategories = savedCategories,
                    purchasedEventIds = purchasedIds,
                    userCity = user.city,
                    today = today
                )
                val nearby = all.filter {
                    it.event.city.equals(user.city, ignoreCase = true) &&
                        !it.event.date.isBefore(today) && it.ticketCount > 0
                }.sortedBy { it.event.date }

                _uiState.update {
                    it.copy(
                        userName = user.name.substringBefore(" "),
                        isLoading = false,
                        recommended = recommended,
                        nearby = nearby
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat rekomendasi")
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectCategory(category: EventCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
