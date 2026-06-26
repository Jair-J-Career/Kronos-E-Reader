package com.kronos.domain.repository

import com.kronos.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getQuotesForBook(bookId: Long): Flow<List<Quote>>
    suspend fun addQuote(quote: Quote): Long
    suspend fun deleteQuote(id: Long)
}
