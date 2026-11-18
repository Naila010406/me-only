package com.example.tiketiin.data.remote

import android.util.Log
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

object EventSummaryTest {

    private const val TAG = "TIKETIIN_SUMMARY"

    suspend fun testSummary() {

        try {

            val remoteDataSource = EventRemoteDataSource()

            val events =
                remoteDataSource.getEventsWithListings()

            Log.d(TAG, "Event Summary berhasil diambil!")
            Log.d(TAG, "Total event: ${events.size}")

            events.forEach { event ->

                val activeListings =
                    requireNotNull(event.ticketListings)
                        .filter {
                            it.status == "AKTIF" &&
                                    it.quantity > 0
                        }

                val lowestPrice =
                    activeListings.minOfOrNull { it.price } ?: 0L

                val ticketCount =
                    activeListings.sumOf { it.quantity }

                Log.d(
                    TAG,
                    "ID: ${event.id} | " +
                            "Event: ${event.name} | " +
                            "Harga: Rp$lowestPrice | " +
                            "Stok: $ticketCount | " +
                            "Listing: ${activeListings.size}"
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: HttpException) {

            Log.e(TAG, "HTTP Error: ${e.code()}")

        } catch (e: Exception) {

            Log.e(TAG, "Event Summary gagal", e)

        }
    }
}