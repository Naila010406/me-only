package com.example.tiketiin.ui.auth

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
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Layar Awal
// ---------------------------------------------------------------------------

@Composable
fun WelcomeScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF3D55C8), BluePrimary, Color(0xFF22368F)))
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            TiketiinLogo(fontSize = 34.sp, baseColor = Color.White)
            Spacer(modifier = Modifier.height(28.dp))

            TicketIllustration(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Beli tiket second,\nnggak bikin was-was lagi",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 30.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Platform jual-beli tiket konser dan event yang aman, fleksibel, dan transparan.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(vertical = 10.dp)
            ) {
                WelcomeFeature(AppIcons.Shield, "Akun terverifikasi e-KYC", Modifier.weight(1f))
                WelcomeFeature(AppIcons.Lock, "Dana ditahan escrow", Modifier.weight(1f))
                WelcomeFeature(AppIcons.Refresh, "Garansi uang kembali 100%", Modifier.weight(1f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Daftar dengan nomor HP",
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlineButton(
                text = "Lanjut dengan Google",
                onClick = onLogin,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row {
                Text(text = "Sudah punya akun? ", color = TextGray, fontSize = 13.sp)
                Text(
                    text = "Masuk",
                    color = BluePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onLogin)
                )
            }
        }
    }
}

@Composable
private fun WelcomeFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

// Ilustrasi tiket sederhana pengganti gambar 3D di Figma.
@Composable
private fun TicketIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF14235F), BluePrimary, GradientPink))
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "ADMIT ONE  •  CAT 2", color = Color.White, fontSize = 10.sp)
            }
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Lalala Festival", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "22 Nov 2026 · 16.00 WIB · JCC", fontSize = 10.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        TicketInfo("GATE", "B")
                        Spacer(modifier = Modifier.width(24.dp))
                        TicketInfo("ROW", "5")
                        Spacer(modifier = Modifier.width(24.dp))
                        TicketInfo("SEAT", "12-13")
                    }
                }
                QrCodeBox(seed = "tiketiin-welcome", modifier = Modifier.size(64.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 18.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(YellowMain),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = AppIcons.Shield, contentDescription = null, tint = TextDark)
                Text(text = "TERVERIFIKASI", fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TicketInfo(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 8.sp, color = TextGray)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

// Garis kemajuan 2 langkah di atas layar daftar dan verifikasi
@Composable
private fun AuthTopBar(step: Int?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 12.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(imageVector = AppIcons.Back, contentDescription = "Kembali")
        }
        if (step != null) {
            Row(modifier = Modifier.align(Alignment.Center)) {
                for (i in 1..2) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(width = 75.dp, height = 5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i == step) BluePrimary else TextGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrDivider() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(TextGray))
        Text(text = "  atau  ", color = TextGray, fontSize = 13.sp)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(TextGray))
    }
}

// ---------------------------------------------------------------------------
// Registrasi
// ---------------------------------------------------------------------------

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: (String) -> Unit,
    onLogin: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Validasi sederhana di sisi tampilan (aturan lengkapnya nanti di fungsi Register).
    fun validate(): String? {
        return when {
            phone.length < 9 -> "Nomor HP belum benar."
            name.isBlank() -> "Nama lengkap wajib diisi."
            !email.contains("@") || !email.contains(".") -> "Format email belum benar."
            password.length < 8 -> "Kata sandi minimal 8 karakter."
            !agree -> "Setujui Syarat & Ketentuan terlebih dahulu."
            else -> null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AuthTopBar(step = 1, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("Buat akun ")
                    appendLogo()
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Daftar sekarang dan mulai temukan tiket event favoritmu!",
                color = TextGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            FormField(label = "Nomor HP") {
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { ch -> ch.isDigit() } },
                    placeholder = "Masukkan no HP",
                    keyboardType = KeyboardType.Phone,
                    leading = { Text(text = "+62", modifier = Modifier.padding(start = 12.dp)) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FormField(label = "Nama Lengkap") {
                AppTextField(value = name, onValueChange = { name = it }, placeholder = "Masukkan nama lengkap")
            }
            Spacer(modifier = Modifier.height(10.dp))
            FormField(label = "Email") {
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Masukkan email",
                    keyboardType = KeyboardType.Email
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FormField(label = "Kata Sandi") {
                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Masukkan kata sandi",
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )
            }
            Text(
                text = "Minimal 8 karakter dengan huruf, angka, dan simbol.",
                color = TextGray,
                fontSize = 9.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agree, onCheckedChange = { agree = it })
                Text(
                    text = buildAnnotatedString {
                        append("Saya setuju dengan ")
                        withStyle(SpanStyle(color = BluePrimary)) { append("Syarat & Ketentuan") }
                        append(" dan ")
                        withStyle(SpanStyle(color = BluePrimary)) { append("Kebijakan Privasi") }
                        append(" tiketiin.")
                    },
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            val message = error
            if (message != null) {
                Text(text = message, color = RedDark, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
            }

            PrimaryButton(
                text = "Daftar",
                onClick = {
                    error = validate()
                    if (error == null) onRegistered(phone)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OrDivider()
            Spacer(modifier = Modifier.height(12.dp))
            OutlineButton(
                text = "Daftar dengan Google",
                onClick = { onRegistered("") },
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(text = "Sudah punya akun? ", color = TextGray, fontSize = 13.sp)
                Text(
                    text = "Masuk",
                    color = BluePrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onLogin)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Verifikasi OTP
// ---------------------------------------------------------------------------

@Composable
fun OtpScreen(
    phone: String,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var secondsLeft by remember { mutableIntStateOf(165) }
    var resendLeft by remember { mutableIntStateOf(30) }

    // Hitung mundur masa berlaku OTP dan tombol kirim ulang.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (secondsLeft > 0) secondsLeft--
            if (resendLeft > 0) resendLeft--
        }
    }

    fun clock(total: Int): String = "%02d:%02d".format(total / 60, total % 60)

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AuthTopBar(step = 2, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(BlueSurface),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(BluePrimary, GradientPink)))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(text = "* * *", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(text = "Verifikasi nomor HP", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Kami telah mengirimkan kode OTP ke\n+62$phone",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Satu kolom isian; 6 kotak di bawahnya menampilkan tiap digit.
            Box {
                OutlinedTextField(
                    value = code,
                    onValueChange = { input -> code = input.filter { it.isDigit() }.take(6) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .alpha(0f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until 6) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp)
                                .border(
                                    1.dp,
                                    if (i == code.length) BluePrimary else LineGray,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = code.getOrNull(i)?.toString() ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = buildAnnotatedString {
                    append("Kode OTP berlaku selama ")
                    withStyle(SpanStyle(color = BluePrimary, fontWeight = FontWeight.Medium)) {
                        append(clock(secondsLeft))
                    }
                },
                color = TextGray,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "Verifikasi",
                onClick = onVerified,
                enabled = code.length == 6,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Tidak menerima kode?", color = TextGray, fontSize = 13.sp)
            Text(
                text = if (resendLeft > 0) "Kirim ulang (${clock(resendLeft)})" else "Kirim ulang",
                color = BluePrimary,
                fontSize = 13.sp,
                modifier = Modifier.clickable(enabled = resendLeft == 0) {
                    secondsLeft = 165
                    resendLeft = 30
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Masuk
// ---------------------------------------------------------------------------

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AuthTopBar(step = null, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Masuk ke ")
                    appendLogo()
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Lanjutkan untuk menemukan tiket event favoritmu, aman dan terpercaya.",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(BluePrimary, GradientPink, GradientOrange))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Ticket,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))

            FormField(label = "Email atau Nomor HP") {
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Masukkan email",
                    keyboardType = KeyboardType.Email
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FormField(label = "Kata Sandi") {
                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Masukkan kata sandi",
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lupa Kata sandi",
                color = BluePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.End)
            )

            val message = error
            if (message != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = message, color = RedDark, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Masuk",
                onClick = {
                    error = when {
                        email.isBlank() -> "Email atau nomor HP wajib diisi."
                        password.length < 8 -> "Kata sandi minimal 8 karakter."
                        else -> null
                    }
                    if (error == null) onLoggedIn()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OrDivider()
            Spacer(modifier = Modifier.height(12.dp))
            OutlineButton(
                text = "Masuk dengan Google",
                onClick = onLoggedIn,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text(text = "Belum punya akun? ", color = TextGray, fontSize = 13.sp)
                Text(
                    text = "Daftar sekarang",
                    color = BluePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onRegister)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
