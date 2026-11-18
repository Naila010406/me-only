package com.example.tiketiin.data.local

import android.content.Context
import android.util.Log
import com.example.tiketiin.data.mapper.toEntity
import com.example.tiketiin.data.remote.EventRemoteDataSource
import kotlinx.coroutines.CancellationException

object EventCacheTest {

    private const val TAG = "TIKETIIN_CACHE"

    suspend fun testCache(context: Context) {

        try {

            val database = DatabaseProvider.getDatabase(context)

            val localDataSource = EventLocalDataSource(
                database.eventDao()
            )

            // Mengambil data dari Supabase
            val remoteDataSource = EventRemoteDataSource()

            val remoteEvents =
                remoteDataSource.getEventsWithListings()

            Log.d(TAG, "Remote events: ${remoteEvents.size}")

            check(remoteEvents.isNotEmpty())

            check(remoteEvents.all { it.ticketListings != null }) {
                "Data listing belum berhasil diambil"
            }

            // Mengubah DTO menjadi Entity
            val entities = remoteEvents.map {
                it.toEntity()
            }

            // Menyimpan data ke Room
            localDataSource.saveEvents(entities)

            // Membaca kembali data dari Room
            val localEvents = localDataSource.getEvents()

            check(localEvents.size == remoteEvents.size)

            Log.d(TAG, "Cache berhasil disimpan!")
            Log.d(TAG, "Local events: ${localEvents.size}")

            localEvents.forEach { event ->

                Log.d(
                    TAG,
                    "LOCAL | ID: ${event.id} | " +
                            "Event: ${event.name} | " +
                            "Harga: Rp${event.lowestPrice} | " +
                            "Stok: ${event.ticketCount}"
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(TAG, "Cache testing gagal", e)

        }
    }

    suspend fun testOfflineCache(context: Context) {

        try {

            val database = DatabaseProvider.getDatabase(context)

            val localDataSource = EventLocalDataSource(
                database.eventDao()
            )

            val localEvents = localDataSource.getEvents()

            check(localEvents.isNotEmpty()) {
                "Local cache masih kosong"
            }

            Log.d(TAG, "Offline cache berhasil dibaca!")
            Log.d(TAG, "Total local events: ${localEvents.size}")

            localEvents.forEach { event ->

                Log.d(
                    TAG,
                    "OFFLINE | ID: ${event.id} | " +
                            "Event: ${event.name} | " +
                            "Harga: Rp${event.lowestPrice} | " +
                            "Stok: ${event.ticketCount}"
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(TAG, "Offline cache gagal", e)

        }
    }
}