package com.example.tiketiin.data.local

import com.example.tiketiin.data.local.dao.EventDao
import com.example.tiketiin.data.local.entity.CachedEventEntity
import kotlinx.coroutines.flow.Flow

class EventLocalDataSource(
    private val eventDao: EventDao
) {

    fun observeEvents(): Flow<List<CachedEventEntity>> {
        return eventDao.observeEvents()
    }

    suspend fun getEvents(): List<CachedEventEntity> {
        return eventDao.getAllEvents()
    }

    suspend fun getEventById(
        eventId: Int
    ): CachedEventEntity? {
        return eventDao.getEventById(eventId)
    }

    suspend fun saveEvents(
        events: List<CachedEventEntity>
    ) {
        eventDao.replaceEvents(events)
    }

    suspend fun clearEvents() {
        eventDao.clearEvents()
    }

    suspend fun countEvents(): Int {
        return eventDao.countEvents()
    }
}