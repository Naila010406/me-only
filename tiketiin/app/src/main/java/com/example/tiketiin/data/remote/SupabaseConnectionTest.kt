package com.example.tiketiin.data.remote

import android.util.Log
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

object SupabaseConnectionTest {

    private const val TAG = "TIKETIIN_API"

    suspend fun testConnection() {

        try {

            val events = RetrofitClient.api.getEvents()

            Log.d(TAG, "Koneksi Supabase berhasil!")
            Log.d(TAG, "Total event: ${events.size}")

            events.forEach { event ->

                Log.d(
                    TAG,
                    "ID: ${event.id} | " +
                            "Event: ${event.name} | " +
                            "Kategori: ${event.category} | " +
                            "Venue: ${event.venue?.name}"
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: HttpException) {

            Log.e(TAG, "HTTP Error: ${e.code()}")

        } catch (e: Exception) {

            Log.e(TAG, "Koneksi gagal", e)

        }
    }
}