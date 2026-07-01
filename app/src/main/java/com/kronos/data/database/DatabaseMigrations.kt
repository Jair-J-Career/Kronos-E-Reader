package com.kronos.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE reading_progress ADD COLUMN is_night_mode INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE reading_progress ADD COLUMN rating INTEGER")
            db.execSQL("ALTER TABLE reading_progress ADD COLUMN review TEXT")
        }
    }
}
