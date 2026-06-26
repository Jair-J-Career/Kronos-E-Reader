package com.kronos.domain.model

data class SearchHistoryItem(
    val id: Long,
    val query: String,
    val searchedAt: Long,
    val resultCount: Int? = null
)
