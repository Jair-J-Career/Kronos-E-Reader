package com.kronos.domain.usecase.search

import com.kronos.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class SaveSearchQueryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke(query: String, resultCount: Int? = null) {
        if (query.isBlank()) return
        searchHistoryRepository.save(query.trim(), resultCount)
    }
}
