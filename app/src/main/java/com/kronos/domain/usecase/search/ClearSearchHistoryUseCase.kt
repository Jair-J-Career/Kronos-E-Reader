package com.kronos.domain.usecase.search

import com.kronos.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class ClearSearchHistoryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke() = searchHistoryRepository.clearAll()
}
