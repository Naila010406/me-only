package com.example.tiketiin.domain

import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventDemandStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// Unit test Fungsi 2 (Hot Opportunity). Jalankan: klik kanan file ini > Run.
class DemandCalculatorTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 16)

    private fun stats(
        id: Int,
        daysFromToday: Long,
        sold: Int,
        wishlist: Int,
        views: Int,
        views7: Int = 0,
        stock: Int = 10
    ): EventDemandStats {
        val event = Event(
            id = id, name = "Event $id", category = EventCategory.MUSIK,
            venue = "Venue", city = "Kota", date = today.plusDays(daysFromToday), time = "19.00",
            latitude = 0.0, longitude = 0.0, description = "",
            posterStart = 0xFF000000, posterEnd = 0xFF000000
        )
        return EventDemandStats(event, sold, wishlist, views, views7, stock)
    }

    @Test
    fun eventTertinggiDiSemuaIndikatorMendapatSkor100() {
        val result = DemandCalculator.calculate(
            listOf(stats(1, 10, sold = 200, wishlist = 100, views = 1000), stats(2, 10, 20, 10, 100)),
            today
        )
        assertEquals(100, result.first { it.event.id == 1 }.demandScore)
    }

    @Test
    fun skorMengikutiBobot50_30_20() {
        // Event 2: terjual 50%, wishlist 50%, view 100% dari nilai tertinggi
        // skor = 100 x (0,5x0,5 + 0,3x0,5 + 0,2x1) = 60
        val result = DemandCalculator.calculate(
            listOf(stats(1, 10, 200, 100, 500), stats(2, 10, 100, 50, 1000)),
            today
        )
        assertEquals(60, result.first { it.event.id == 2 }.demandScore)
    }

    @Test
    fun hasilDiurutkanDariSkorTertinggi() {
        val result = DemandCalculator.calculate(
            listOf(stats(1, 10, 10, 5, 50), stats(2, 10, 200, 100, 1000), stats(3, 10, 100, 50, 500)),
            today
        )
        assertEquals(listOf(2, 3, 1), result.map { it.event.id })
    }

    @Test
    fun eventYangSudahLewatDisaring() {
        val result = DemandCalculator.calculate(
            listOf(stats(1, -1, 500, 500, 5000), stats(2, 0, 10, 10, 10)),
            today
        )
        assertEquals(listOf(2), result.map { it.event.id })
    }

    @Test
    fun hanyaSkorDiAtasThresholdYangHighDemand() {
        val result = DemandCalculator.highDemandOnly(
            listOf(stats(1, 10, 200, 100, 1000), stats(2, 10, 100, 50, 1000), stats(3, 10, 20, 10, 100)),
            today
        )
        // Event 2 skornya tepat 60 (= threshold) sehingga ikut masuk; event 3 tidak.
        assertEquals(listOf(1, 2), result.map { it.event.id })
        assertTrue(result.all { it.isHighDemand })
    }

    @Test
    fun pencariAktifDanLabelStok() {
        val item = DemandCalculator.calculate(
            listOf(stats(1, 10, 100, wishlist = 96, views = 1000, views7 = 54, stock = 16)),
            today
        ).first()
        assertEquals(150, item.activeSeekers)
        assertEquals("<20", item.stockLabel)
        assertTrue(item.isLowStock)
        // peluang laku = 150 / (150 + 16) = 90%
        assertEquals(90, item.sellChancePercent)
    }

    @Test
    fun stokBanyakDitampilkanApaAdanya() {
        val item = DemandCalculator.calculate(listOf(stats(1, 10, 100, 10, 100, stock = 35)), today).first()
        assertEquals("35", item.stockLabel)
        assertFalse(item.isLowStock)
    }

    @Test
    fun dataKosongTidakError() {
        assertTrue(DemandCalculator.calculate(emptyList(), today).isEmpty())
        // Semua indikator nol tidak boleh menyebabkan pembagian dengan nol.
        val zero = DemandCalculator.calculate(listOf(stats(1, 5, 0, 0, 0, 0, 0)), today).first()
        assertEquals(0, zero.demandScore)
        assertEquals(0, zero.sellChancePercent)
    }
}
