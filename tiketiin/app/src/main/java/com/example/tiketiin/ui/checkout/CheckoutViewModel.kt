package com.example.tiketiin.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.repository.TiketiinRepository
import com.example.tiketiin.domain.HotelFinder
import com.example.tiketiin.domain.NearbyHotel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

val PAYMENT_METHODS = listOf("Transfer Virtual Account", "Kartu Kredit / Debit", "E-wallet dan QRIS")

data class CheckoutUiState(
    val event: Event? = null,
    val listing: TicketListing? = null,
    val quantity: Int = 1,
    // Isi formulir pesanan
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val idNumber: String = "",
    val address: String = "",
    val powerOfAttorney: Boolean = false,
    val formError: String? = null,
    // Pembayaran
    val paymentMethod: String = PAYMENT_METHODS.first(),
    val secondsLeft: Int = LOCK_SECONDS,
    val isPaying: Boolean = false,
    val payError: String? = null
) {
    val ticketPrice: Long get() = (listing?.price ?: 0L) * quantity
    val serviceFee: Long get() = ticketPrice * 5 / 100
    val total: Long get() = ticketPrice + serviceFee
    val isExpired: Boolean get() = secondsLeft <= 0
    val seatLabel: String get() = listing?.let { "${it.seatCategory}, ${it.row}" } ?: "-"

    companion object {
        // Tiket dikunci sementara 10 menit supaya tidak dibeli dua orang sekaligus.
        const val LOCK_SECONDS = 600
    }
}

/**
 * Dipakai bersama oleh Form Pesanan dan Pembayaran (dibuat di TiketiinApp,
 * jadi datanya tetap ada saat pindah di antara dua layar itu).
 */
class CheckoutViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // Dipanggil saat tombol "Beli Tiket" ditekan.
    fun start(eventId: Int, quantity: Int) {
        viewModelScope.launch {
            val user = repository.getUser()
            val summary = repository.getEventSummary(eventId)
            val listing = repository.getCheapestListing(eventId)
            _uiState.value = CheckoutUiState(
                event = summary?.event,
                listing = listing,
                quantity = quantity.coerceIn(1, (listing?.quantity ?: 1).coerceAtLeast(1)),
                name = user.name,
                email = user.email,
                phone = user.phone
            )
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value) }
    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value) }
    fun updateIdNumber(value: String) = _uiState.update { it.copy(idNumber = value) }
    fun updateAddress(value: String) = _uiState.update { it.copy(address = value) }
    fun togglePowerOfAttorney() = _uiState.update { it.copy(powerOfAttorney = !it.powerOfAttorney) }
    fun selectPaymentMethod(method: String) = _uiState.update { it.copy(paymentMethod = method) }

    // true kalau formulir lengkap.
    fun validateForm(): Boolean {
        val state = _uiState.value
        val error = when {
            state.name.isBlank() -> "Nama wajib diisi."
            !state.email.contains("@") -> "Format email belum benar."
            state.phone.length < 9 -> "Nomor HP belum benar."
            state.idNumber.length < 8 -> "Nomor kartu identitas belum benar."
            state.address.isBlank() -> "Alamat wajib diisi."
            else -> null
        }
        _uiState.update { it.copy(formError = error) }
        return error == null
    }

    fun pay(onSuccess: (Int) -> Unit) {
        val state = _uiState.value
        val listing = state.listing ?: return
        if (state.isExpired || state.isPaying) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true, payError = null) }
            try {
                val order = repository.createOrder(
                    listingId = listing.id,
                    quantity = state.quantity,
                    buyerName = state.name,
                    paymentMethod = state.paymentMethod
                )
                timerJob?.cancel()
                _uiState.update { it.copy(isPaying = false) }
                onSuccess(order.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isPaying = false, payError = e.message ?: "Pembayaran gagal")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Layar Pembayaran Berhasil + Rekomendasi Hotel
// ---------------------------------------------------------------------------

data class PaymentSuccessUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val event: Event? = null,
    val hotels: List<NearbyHotel> = emptyList()
)

class PaymentSuccessViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentSuccessUiState())
    val uiState: StateFlow<PaymentSuccessUiState> = _uiState.asStateFlow()

    fun load(orderId: Int) {
        viewModelScope.launch {
            val order = repository.getOrder(orderId)
            val event = order?.let { repository.getEventSummary(it.eventId)?.event }
            // Fungsi 4: hotel terdekat dari venue event yang baru dibeli.
            val hotels = if (event != null) {
                HotelFinder.findNearby(event.latitude, event.longitude, repository.getHotels())
            } else {
                emptyList()
            }
            _uiState.value = PaymentSuccessUiState(
                isLoading = false,
                order = order,
                event = event,
                hotels = hotels
            )
        }
    }
}
