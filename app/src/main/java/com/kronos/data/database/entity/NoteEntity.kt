package com.kronos.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["linked_quote_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("book_id"), Index("linked_quote_id")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "page_number") val pageNumber: Int? = null,
    val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "linked_quote_id") val linkedQuoteId: Long? = null,
    @ColumnInfo(name = "embedding_id") val embeddingId: String? = null,
    @ColumnInfo(name = "source_text_hash") val sourceTextHash: String? = null
)
