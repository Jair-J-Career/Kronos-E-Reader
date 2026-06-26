package com.kronos.domain.usecase.search

import com.kronos.domain.model.SearchHistoryItem
import com.kronos.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSearchHistoryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    operator fun invoke(): Flow<List<SearchHistoryItem>> =
        searchHistoryRepository.getRecentQueries()
}
