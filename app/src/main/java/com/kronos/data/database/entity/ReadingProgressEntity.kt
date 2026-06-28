package com.kronos.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kronos.domain.model.ReadingStatus

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"], unique = true)]
)
data class ReadingProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "status", defaultValue = "TO_READ") val status: ReadingStatus = ReadingStatus.TO_READ,
    @ColumnInfo(name = "current_page", defaultValue = "0") val currentPage: Int = 0,
    @ColumnInfo(name = "read_percentage", defaultValue = "0.0") val readPercentage: Double = 0.0,
    @ColumnInfo(name = "started_at") val startedAt: Long? = null,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_night_mode", defaultValue = "0") val isNightMode: Boolean = false
)
