package com.kronos.domain.model

data class Quote(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val embeddingId: String? = null,
    val sourceTextHash: String? = null
)
