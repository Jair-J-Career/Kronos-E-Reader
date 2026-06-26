package com.kronos.domain.model

data class Note(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int? = null,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val linkedQuoteId: Long? = null,
    val embeddingId: String? = null,
    val sourceTextHash: String? = null
)
