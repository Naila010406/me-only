package com.example.tiketiin.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiketiin.data.model.*
import com.example.tiketiin.domain.*
import com.example.tiketiin.ui.components.*
import com.example.tiketiin.ui.theme.*

@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onSelectTab: (Int) -> Unit,
    onSelectStatus: (OrderStatus) -> Unit,
    onMoveTo: (Int, OrderStatus) -> Unit,
    onRate: (Int, Int) -> Unit,
    onOpenTicket: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(BlueSoft)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 14.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Orders", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderTypeTab(label = "Order Saya", selected = uiState.selectedTab == 0, onClick = { onSelectTab(0) })
                OrderTypeTab(label = "Order Toko", selected = uiState.selectedTab == 1, onClick = { onSelectTab(1) })
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
        ) {
            // Tab status pesanan
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                OrderStatus.entries.forEach { status ->
                    val selected = uiState.selectedStatus == status
                    val count = uiState.countFor(status)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectStatus(status) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (count > 0) "${status.label} ($count)" else status.label,
                            fontSize = 8.sp,
                            maxLines = 1,
                            color = if (selected) BluePrimary else TextDark,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(if (selected) BluePrimary else TextGray)
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> LoadingBox()
                uiState.visibleOrders.isEmpty() -> EmptyBox(text = "Belum ada pesanan pada status ini.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.visibleOrders) { item ->
                        if (uiState.selectedTab == 0) {
                            MyOrderCard(item = item, onMoveTo = onMoveTo, onRate = onRate, onOpenTicket = onOpenTicket)
                        } else {
                            StoreOrderCard(item = item, onMoveTo = onMoveTo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderTypeTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color.White else LineGray)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) BluePrimary else TextGray
        )
    }
}

@Composable
private fun OrderInfo(item: OrderItem) {
    val event = item.event
    Text(
        text = event?.name ?: "Event dihapus",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
    if (event != null) {
        IconText(icon = AppIcons.Calendar, text = formatDateShort(event.date))
        Row {
            IconText(
                icon = AppIcons.Location,
                text = "${event.venue}, ${event.city}",
                modifier = Modifier.weight(1f)
            )
            IconText(icon = AppIcons.Ticket, text = item.order.quantity.toString())
        }
    }
    IconText(icon = AppIcons.Seat, text = item.order.seatLabel)
}

// Kartu pesanan pembeli. Isinya menyesuaikan status pesanan.
@Composable
private fun MyOrderCard(
    item: OrderItem,
    onMoveTo: (Int, OrderStatus) -> Unit,
    onRate: (Int, Int) -> Unit,
    onOpenTicket: (Int) -> Unit
) {
    val order = item.order
    WhiteCard(modifier = Modifier.fillMaxWidth().clickable { onOpenTicket(order.id) }) {
        OrderInfo(item = item)
        Spacer(modifier = Modifier.height(8.dp))
        when (order.status) {
            OrderStatus.BELUM_BAYAR -> {
                Text(text = "Menunggu pembayaran.", fontSize = 11.sp, color = TextGray)
            }
            OrderStatus.DIPROSES -> {
                Text(
                    text = "Dana ${formatRupiah(order.total)} ditahan (escrow). Menunggu penjual mengirim tiket.",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlineButton(
                    text = "Tiket sudah saya terima",
                    onClick = { onMoveTo(order.id, OrderStatus.DITERIMA) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OrderStatus.DITERIMA -> {
                QrCodeBox(
                    seed = order.transactionNo,
                    modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Konfirmasi tiket valid",
                    onClick = { onMoveTo(order.id, OrderStatus.SELESAI) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OrderStatus.SELESAI -> {
                QrCodeBox(
                    seed = order.transactionNo,
                    modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    if (order.status == OrderStatus.DITERIMA || order.status == OrderStatus.SELESAI) {
        Spacer(modifier = Modifier.height(10.dp))
        val canRate = order.status == OrderStatus.SELESAI
        WhiteCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Beri Penilaian", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = if (canRate) "Ketuk bintang untuk menilai penjual" else "Aktif setelah kamu konfirmasi tiket",
                        fontSize = 10.sp,
                        color = TextGray
                    )
                }
                for (star in 1..5) {
                    Icon(
                        imageVector = if (star <= order.rating) AppIcons.Star else AppIcons.StarBorder,
                        contentDescription = "Bintang $star",
                        tint = if (star <= order.rating) YellowMain else TextGray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(enabled = canRate) { onRate(order.id, star) }
                    )
                }
            }
        }
    }
}

// Kartu pesanan yang masuk ke toko (sisi penjual)
@Composable
private fun StoreOrderCard(
    item: OrderItem,
    onMoveTo: (Int, OrderStatus) -> Unit
) {
    val order = item.order
    WhiteCard(modifier = Modifier.fillMaxWidth()) {
        OrderInfo(item = item)
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = LineGray)
        DetailRow(label = "Pembeli", value = order.buyerName)
        DetailRow(label = "No. Transaksi", value = order.transactionNo)
        DetailRow(label = "Kamu terima", value = formatRupiah(order.ticketPrice), bold = true)
        when (order.status) {
            OrderStatus.DIPROSES -> {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Kirim tiket ke pembeli",
                    onClick = { onMoveTo(order.id, OrderStatus.DITERIMA) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OrderStatus.DITERIMA -> {
                Text(
                    text = "Tiket terkirim. Dana cair setelah pembeli mengonfirmasi.",
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
            OrderStatus.SELESAI -> {
                Text(
                    text = if (order.rating > 0) "Selesai. Penilaian pembeli: ${order.rating}/5" else "Selesai. Dana sudah cair.",
                    fontSize = 10.sp,
                    color = GreenMain
                )
            }
            OrderStatus.BELUM_BAYAR -> {
                Text(text = "Menunggu pembayaran pembeli.", fontSize = 10.sp, color = TextGray)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Detail Tiket (Lihat Tiket Saya): QR besar + hitung mundur menuju event
// ---------------------------------------------------------------------------

@Composable
fun TicketDetailScreen(
    uiState: TicketUiState,
    onBack: () -> Unit
) {
    val item = uiState.item
    Column(modifier = Modifier.fillMaxSize().background(ScreenGray)) {
        ScreenTopBar(title = "Tiket Saya", onBack = onBack)
        if (uiState.isLoading) {
            LoadingBox()
        } else if (item == null) {
            EmptyBox(text = "Tiket tidak ditemukan.")
        } else {
            val order = item.order
            val event = item.event
            val hasTicket = order.status == OrderStatus.DITERIMA || order.status == OrderStatus.SELESAI
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (event != null) {
                    PosterBox(
                        event = event,
                        titleSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                WhiteCard(modifier = Modifier.fillMaxWidth()) {
                    OrderInfo(item = item)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (item.daysToEvent >= 0) BlueSurface else FieldGray)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                item.daysToEvent > 0 -> "Event dimulai ${item.daysToEvent} hari lagi"
                                item.daysToEvent == 0L -> "Event berlangsung hari ini"
                                else -> "Event sudah selesai"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.daysToEvent >= 0) BluePrimary else TextGray
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (hasTicket) {
                        QrCodeBox(
                            seed = order.transactionNo,
                            modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Tunjukkan kode ini di pintu masuk venue.",
                            fontSize = 10.sp,
                            color = TextGray,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                        )
                    } else {
                        Text(
                            text = "Kode QR muncul setelah tiket dikirim penjual.",
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                WhiteCard(modifier = Modifier.fillMaxWidth()) {
                    DetailRow(label = "Status", value = order.status.label)
                    DetailRow(label = "Nama Pemesan", value = order.buyerName)
                    DetailRow(label = "Metode Pembayaran", value = order.paymentMethod)
                    DetailRow(label = "No. Transaksi", value = order.transactionNo)
                    DetailRow(label = "Waktu", value = order.createdAt)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = LineGray)
                    DetailRow(label = "Harga Tiket", value = formatRupiah(order.ticketPrice))
                    DetailRow(label = "Biaya Layanan (5%)", value = formatRupiah(order.serviceFee))
                    DetailRow(label = "Total", value = formatRupiah(order.total), bold = true)
                }
            }
        }
    }
}
