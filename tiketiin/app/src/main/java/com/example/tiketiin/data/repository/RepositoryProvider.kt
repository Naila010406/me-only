package com.example.tiketiin.data.repository

import android.content.Context
import com.example.tiketiin.data.local.DatabaseProvider
import com.example.tiketiin.data.local.EventLocalDataSource
import com.example.tiketiin.data.remote.EventRemoteDataSource

object RepositoryProvider {

    @Volatile
    private var eventRepository:
            EventCatalogRepository? = null

    fun getEventRepository(
        context: Context
    ): EventCatalogRepository {

        return eventRepository ?: synchronized(this) {

            eventRepository ?: EventCatalogRepository(

                localDataSource = EventLocalDataSource(
                    DatabaseProvider
                        .getDatabase(context)
                        .eventDao()
                ),

                remoteDataSource = EventRemoteDataSource()

            ).also {
                eventRepository = it
            }
        }
    }
}