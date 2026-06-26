package com.kronos.domain.model

data class Bookmark(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val label: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val embeddingId: String? = null,
    val sourceTextHash: String? = null
)
