package com.example.tiketiin.data.mapper

import com.example.tiketiin.data.local.entity.CachedEventEntity
import com.example.tiketiin.data.remote.dto.EventDto

fun EventDto.toEntity(): CachedEventEntity {

    return CachedEventEntity(
        id = id,
        name = name,
        category = category,
        venueId = venueId,
        venueName = venue?.name,
        city = venue?.city,
        date = date,
        time = time,
        description = description,
        posterUrl = posterUrl,
        posterStart = posterStart,
        posterEnd = posterEnd,
        flashEndsAt = flashEndsAt,
        baseSold = baseSold,
        baseWishlist = baseWishlist,
        viewCount = viewCount,
        viewsLast7Days = viewsLast7Days,
        cachedAt = System.currentTimeMillis()
    )
}