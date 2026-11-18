package com.example.tiketiin.domain

import java.time.LocalDate
import java.util.Locale

private val BULAN_PENDEK = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)
private val BULAN_PANJANG = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

// 1300000 -> "Rp1.300.000"
fun formatRupiah(amount: Long): String {
    return "Rp" + "%,d".format(Locale.US, amount).replace(',', '.')
}

// 2026-10-17 -> "17 Okt 2026"
fun formatDateShort(date: LocalDate): String {
    return "${date.dayOfMonth} ${BULAN_PENDEK[date.monthValue - 1]} ${date.year}"
}

// 2026-10-17 -> "17 Oktober 2026"
fun formatDateLong(date: LocalDate): String {
    return "${date.dayOfMonth} ${BULAN_PANJANG[date.monthValue - 1]} ${date.year}"
}

// 8 -> "Berakhir 8 jam lagi", 120 -> "Berakhir 5 hari lagi"
fun formatFlashLabel(hours: Int): String {
    return if (hours < 24) "Berakhir $hours jam lagi" else "Berakhir ${hours / 24} hari lagi"
}

fun formatClosingLabel(hours: Int): String {
    return if (hours < 24) {
        "Pembelian tiket ditutup dalam $hours jam"
    } else {
        "Pembelian tiket ditutup dalam ${hours / 24} hari"
    }
}
