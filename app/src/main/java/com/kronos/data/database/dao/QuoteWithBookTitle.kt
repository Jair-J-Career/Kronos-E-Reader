package com.kronos.data.database.dao

import androidx.room.ColumnInfo

data class QuoteWithBookTitle(
    val id: Long,
    @ColumnInfo(name = "book_id") val bookId: Long,
    val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "book_title") val bookTitle: String
)
