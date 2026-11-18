package com.example.tiketiin.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Pesanan + event-nya + sisa hari menuju event.
data class OrderItem(
    val order: Order,
    val event: Event?,
    val daysToEvent: Long
)

data class OrdersUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,                               // 0 = Order Saya, 1 = Order Toko
    val selectedStatus: OrderStatus = OrderStatus.DIPROSES,
    val myOrders: List<OrderItem> = emptyList(),
    val storeOrders: List<OrderItem> = emptyList()
) {
    // Pesanan dikelompokkan menurut status yang dipilih.
    val visibleOrders: List<OrderItem>
        get() {
            val source = if (selectedTab == 0) myOrders else storeOrders
            return source.filter { it.order.status == selectedStatus }
        }

    fun countFor(status: OrderStatus): Int {
        val source = if (selectedTab == 0) myOrders else storeOrders
        return source.count { it.order.status == status }
    }
}

class OrdersViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.myOrders.isEmpty() && it.storeOrders.isEmpty()) }
            val my = repository.getMyOrders().map { toItem(it) }
            val store = repository.getStoreOrders().map { toItem(it) }
            _uiState.update { it.copy(isLoading = false, myOrders = my, storeOrders = store) }
        }
    }

    private suspend fun toItem(order: Order): OrderItem {
        val event = repository.getEventSummary(order.eventId)?.event
        val days = if (event != null) ChronoUnit.DAYS.between(LocalDate.now(), event.date) else 0L
        return OrderItem(order, event, days)
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectStatus(status: OrderStatus) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    // Pindahkan pesanan ke status berikutnya, lalu ikut pindah ke tab status itu.
    fun moveTo(orderId: Int, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            _uiState.update { it.copy(selectedStatus = status) }
            load()
        }
    }

    fun rate(orderId: Int, rating: Int) {
        viewModelScope.launch {
            repository.rateOrder(orderId, rating)
            load()
        }
    }
}

// ---------------------------------------------------------------------------
// Detail Tiket (Lihat Tiket Saya)
// ---------------------------------------------------------------------------

data class TicketUiState(
    val isLoading: Boolean = true,
    val item: OrderItem? = null
)

class TicketViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    fun load(orderId: Int) {
        viewModelScope.launch {
            val order = repository.getOrder(orderId)
            val event = order?.let { repository.getEventSummary(it.eventId)?.event }
            val days = if (event != null) ChronoUnit.DAYS.between(LocalDate.now(), event.date) else 0L
            _uiState.value = TicketUiState(
                isLoading = false,
                item = order?.let { OrderItem(it, event, days) }
            )
        }
    }
}
