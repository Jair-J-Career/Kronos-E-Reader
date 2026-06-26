package com.kronos.domain.model

data class PageTextChunk(
    val bookId: Long,
    val pageNumber: Int,
    val text: String,
    val hash: String
)
