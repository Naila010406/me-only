package com.example.tiketiin.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException

object EventRepositoryTest {

    private const val TAG = "TIKETIIN_REPOSITORY"

    suspend fun testRepository(context: Context) {

        try {

            val repository =
                RepositoryProvider.getEventRepository(context)

            val cachedEvents =
                repository.getCachedEvents()

            Log.d(
                TAG,
                "Cache awal: ${cachedEvents.size} event"
            )

            repository.refreshEvents()

            val updatedEvents =
                repository.getCachedEvents()

            Log.d(TAG, "Repository berhasil!")
            Log.d(
                TAG,
                "Total event: ${updatedEvents.size}"
            )

            updatedEvents.forEach { summary ->

                Log.d(
                    TAG,
                    "Event: ${summary.event.name} | " +
                            "Harga: ${summary.lowestPrice} | " +
                            "Stok: ${summary.ticketCount}"
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(TAG, "Repository gagal", e)

        }
    }
}