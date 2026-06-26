package com.kronos.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["series_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("is_in_trash"),
        Index("is_favorite"),
        Index("series_id"),
        Index("last_opened_at")
    ]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "file_uri") val fileUri: String,
    @ColumnInfo(name = "file_size_bytes") val fileSizeBytes: Long,
    @ColumnInfo(name = "page_count") val pageCount: Int,
    @ColumnInfo(name = "cover_image_path") val coverImagePath: String? = null,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "last_opened_at") val lastOpenedAt: Long? = null,
    @ColumnInfo(name = "is_favorite", defaultValue = "0") val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_in_trash", defaultValue = "0") val isInTrash: Boolean = false,
    @ColumnInfo(name = "trashed_at") val trashedAt: Long? = null,
    @ColumnInfo(name = "series_id") val seriesId: Long? = null,
    @ColumnInfo(name = "series_position") val seriesPosition: Double? = null,
    @ColumnInfo(name = "embedding_id") val embeddingId: String? = null,
    @ColumnInfo(name = "source_text_hash") val sourceTextHash: String? = null
)
