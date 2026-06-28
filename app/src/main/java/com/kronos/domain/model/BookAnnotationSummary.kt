package com.kronos.domain.model

data class BookAnnotationSummary(
    val bookId: Long,
    val title: String,
    val coverImagePath: String?,
    val quoteCount: Int,
    val noteCount: Int,
    val latestAnnotationAt: Long
)
