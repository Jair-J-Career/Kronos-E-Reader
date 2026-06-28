package com.kronos.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithAnnotations(
    @Embedded val book: BookEntity,
    @Relation(parentColumn = "id", entityColumn = "book_id") val quotes: List<QuoteEntity>,
    @Relation(parentColumn = "id", entityColumn = "book_id") val notes: List<NoteEntity>
)
