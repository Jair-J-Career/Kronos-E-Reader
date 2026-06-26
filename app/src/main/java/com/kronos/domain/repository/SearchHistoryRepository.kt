package com.kronos.domain.repository

import com.kronos.domain.model.SearchHistoryItem
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getRecentQueries(limit: Int = 20): Flow<List<SearchHistoryItem>>
    suspend fun save(query: String, resultCount: Int? = null)
    suspend fun deleteById(id: Long)
    suspend fun clearAll()
}
