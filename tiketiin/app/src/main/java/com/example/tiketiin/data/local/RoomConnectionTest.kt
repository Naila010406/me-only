package com.example.tiketiin.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.tiketiin.data.local.entity.CachedEventEntity
import kotlinx.coroutines.CancellationException

object RoomConnectionTest {

    private const val TAG = "TIKETIIN_ROOM"

    suspend fun testConnection(context: Context) {

        val database = Room.databaseBuilder(
            context.applicationContext,
            TiketiinDatabase::class.java,
            "tiketiin_test_database"
        )
            .addMigrations(
                DatabaseMigrations.MIGRATION_1_2,
                DatabaseMigrations.MIGRATION_2_3
            )
            .build()

        try {

            val dao = database.eventDao()

            val testEvent = CachedEventEntity(
                id = -1,
                name = "TEST ROOM DATABASE",
                category = "MUSIK",
                venueId = 1,
                venueName = "Test Venue",
                city = "Malang",
                date = "2027-01-01",
                time = "19:00:00",
                description = "Testing local database",
                posterUrl = null,
                posterStart = 0L,
                posterEnd = 0L,
                flashEndsAt = null,
                baseSold = 0,
                baseWishlist = 0,
                viewCount = 0,
                viewsLast7Days = 0
            )

            dao.replaceEvents(listOf(testEvent))

            val savedEvent = dao.getEventById(-1)

            check(savedEvent == testEvent)
            check(dao.countEvents() == 1)

            Log.d(TAG, "Room Database berhasil!")
            Log.d(TAG, "Insert data berhasil")
            Log.d(TAG, "Total event: ${dao.countEvents()}")
            Log.d(TAG, "Event: ${savedEvent?.name}")

            dao.clearEvents()

            check(dao.countEvents() == 0)

            Log.d(TAG, "Delete data berhasil")
            Log.d(TAG, "Total event akhir: ${dao.countEvents()}")

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(TAG, "Room Database gagal", e)

        } finally {

            database.close()

        }
    }
}