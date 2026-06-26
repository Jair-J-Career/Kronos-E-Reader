package com.kronos.domain.usecase.quote

import com.kronos.domain.model.Quote
import com.kronos.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuotesForBookUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    operator fun invoke(bookId: Long): Flow<List<Quote>> = repository.getQuotesForBook(bookId)
}
