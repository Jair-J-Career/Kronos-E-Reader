package com.kronos.domain.model

data class QuoteSummary(
    val id: Long,
    val bookId: Long,
    val bookTitle: String,
    val text: String,
    val createdAt: Long
)
