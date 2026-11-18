package com.example.tiketiin.ui.profile

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val purchaseCount: Int = 0,
    val ticketCount: Int = 0,
    val spending: Long = 0L
) {
    // Naik level Gold setelah belanja Rp10.000.000 (aturan contoh).
    val levelProgress: Float get() = (spending / 10_000_000f).coerceIn(0.05f, 1f)
    val discountSaved: Long get() = spending / 20
}

class ProfileViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val orders = repository.getMyOrders()
            _uiState.value = ProfileUiState(
                user = repository.getUser(),
                purchaseCount = orders.size,
                ticketCount = orders.sumOf { it.quantity },
                spending = orders.sumOf { it.total }
            )
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onDone()
        }
    }
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onOpenSaved: () -> Unit,
    onLogout: () -> Unit
) {
    val user = uiState.user
    var showLogout by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.shadow(4.dp)) {
            ScreenTopBar(title = "Profil")
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BluePrimary, GradientPink))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.name?.take(1) ?: "",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = user?.name ?: "", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = AppIcons.Edit, contentDescription = "Ubah nama", tint = BluePrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(TextGray, LineGray)))
                    .padding(horizontal = 26.dp, vertical = 2.dp)
            ) {
                Text(text = user?.level ?: "", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Level Progress", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LineGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(uiState.levelProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(YellowMain)
                )
            }
            Text(text = "Gold", fontSize = 9.sp, color = TextGray, modifier = Modifier.align(Alignment.End))

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(
                    value = "10%",
                    label = "Diskon ${user?.level ?: ""}",
                    background = YellowSoft,
                    valueColor = Color(0xFF4A3500),
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    value = formatRupiah(uiState.discountSaved),
                    label = "Total hemat",
                    background = BlueSoft,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Ringkasan Aktivitas", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BlueSoft)
            ) {
                MiniStat(uiState.purchaseCount.toString(), "Pembelian", Modifier.weight(1f), Color.Transparent)
                MiniStat(uiState.ticketCount.toString(), "Tiket dibeli", Modifier.weight(1f), Color.Transparent)
                MiniStat(formatRupiah(uiState.spending), "Pengeluaran", Modifier.weight(1.3f), Color.Transparent)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ScreenGray)
            ) {
                ProfileMenuRow(icon = AppIcons.BookmarkBorder, label = "Event Tersimpan", onClick = onOpenSaved)
                HorizontalDivider(color = LineGray)
                ProfileMenuRow(
                    icon = AppIcons.Shield,
                    label = "Kode Referral",
                    trailingText = user?.referralCode,
                    onClick = {}
                )
                HorizontalDivider(color = LineGray)
                ProfileMenuRow(icon = AppIcons.Info, label = "Pusat Bantuan", onClick = {})
                HorizontalDivider(color = LineGray)
                ProfileMenuRow(icon = AppIcons.Settings, label = "Pengaturan akun", onClick = {})
                HorizontalDivider(color = LineGray)
                ProfileMenuRow(
                    icon = AppIcons.Logout,
                    label = "Keluar",
                    tint = RedDark,
                    onClick = { showLogout = true }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogout) {
        LogoutDialog(
            onConfirm = {
                showLogout = false
                onLogout()
            },
            onDismiss = { showLogout = false }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = BluePrimary,
    trailingText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 14.sp, color = if (tint == RedDark) RedDark else Color(0xFF4A4A4A), modifier = Modifier.weight(1f))
        if (trailingText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(YellowSoft)
                    .padding(horizontal = 14.dp, vertical = 1.dp)
            ) {
                Text(text = trailingText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Icon(imageVector = AppIcons.ChevronRight, contentDescription = null, tint = Color(0xFF4A4A4A))
        }
    }
}

@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(RedSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = AppIcons.Logout, contentDescription = null, tint = RedDark, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Keluar dari akun?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kamu perlu masuk kembali untuk membeli atau menjual tiket. Pesanan yang sedang berjalan tetap aman.",
                fontSize = 13.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = "Ya, Keluar",
                onClick = onConfirm,
                containerColor = RedDark,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlineButton(text = "Batal", onClick = onDismiss, color = TextDark, modifier = Modifier.fillMaxWidth())
        }
    }
}
