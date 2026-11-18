package com.example.tiketiin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tiketiin.data.local.dao.EventDao
import com.example.tiketiin.data.local.entity.CachedEventEntity

@Database(
    entities = [
        CachedEventEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class TiketiinDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

}