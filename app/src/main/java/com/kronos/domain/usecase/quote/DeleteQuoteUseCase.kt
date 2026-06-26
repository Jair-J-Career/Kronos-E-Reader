package com.kronos.domain.usecase.quote

import com.kronos.domain.repository.QuoteRepository
import javax.inject.Inject

class DeleteQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteQuote(id)
}
