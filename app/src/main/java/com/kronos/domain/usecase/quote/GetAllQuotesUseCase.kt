package com.kronos.domain.usecase.quote

import com.kronos.domain.model.QuoteSummary
import com.kronos.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllQuotesUseCase @Inject constructor(private val repository: QuoteRepository) {
    operator fun invoke(): Flow<List<QuoteSummary>> = repository.getAllQuotes()
}
