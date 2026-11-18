package com.example.tiketiin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_events")
data class CachedEventEntity(

    @PrimaryKey
    val id: Int,

    val name: String,
    val category: String,
    val venueId: Int,
    val venueName: String?,
    val city: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val date: String,
    val time: String,
    val description: String,
    val posterUrl: String?,
    val posterStart: Long,
    val posterEnd: Long,
    val flashEndsAt: String?,
    val baseSold: Int,
    val baseWishlist: Int,
    val viewCount: Int,
    val viewsLast7Days: Int,

    val lowestPrice: Long? = null,
    val ticketCount: Int? = null,

    val cachedAt: Long = System.currentTimeMillis()

)