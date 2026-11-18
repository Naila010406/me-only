package com.example.tiketiin.data.remote.api

import com.example.tiketiin.data.remote.dto.EventDto
import com.example.tiketiin.data.remote.dto.VenueDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseApiService {

    // Mengambil seluruh data event
    @GET("rest/v1/events")
    suspend fun getEvents(
        @Query("select")
        select: String = "*,venue:venues(*)",

        @Query("order")
        order: String = "date.asc"
    ): List<EventDto>


    // Mengambil detail event berdasarkan ID
    @GET("rest/v1/events")
    suspend fun getEventById(
        @Query("id")
        id: String,

        @Query("select")
        select: String = "*,venue:venues(*)"
    ): List<EventDto>


    // Mengambil seluruh data venue
    @GET("rest/v1/venues")
    suspend fun getVenues(
        @Query("select")
        select: String = "*"
    ): List<VenueDto>

}