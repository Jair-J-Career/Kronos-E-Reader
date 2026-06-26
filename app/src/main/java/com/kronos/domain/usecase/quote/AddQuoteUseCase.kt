package com.kronos.domain.usecase.quote

import com.kronos.domain.model.Quote
import com.kronos.domain.repository.QuoteRepository
import javax.inject.Inject

class AddQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quote: Quote): Long = repository.addQuote(quote)
}
