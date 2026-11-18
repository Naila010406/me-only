package com.example.tiketiin.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN latitude REAL"
            )

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN longitude REAL"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN lowestPrice INTEGER"
            )

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN ticketCount INTEGER"
            )
        }
    }
}