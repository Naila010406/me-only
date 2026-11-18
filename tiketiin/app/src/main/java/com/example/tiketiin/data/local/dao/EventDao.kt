package com.example.tiketiin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tiketiin.data.local.entity.CachedEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM cached_events ORDER BY date ASC")
    fun observeEvents(): Flow<List<CachedEventEntity>>

    @Query("SELECT * FROM cached_events ORDER BY date ASC")
    suspend fun getAllEvents(): List<CachedEventEntity>

    @Query("SELECT * FROM cached_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Int): CachedEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CachedEventEntity>)

    @Query("DELETE FROM cached_events")
    suspend fun clearEvents()

    @Query("SELECT COUNT(*) FROM cached_events")
    suspend fun countEvents(): Int

    @Transaction
    suspend fun replaceEvents(events: List<CachedEventEntity>) {
        clearEvents()
        insertEvents(events)
    }

}