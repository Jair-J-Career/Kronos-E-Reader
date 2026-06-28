package com.kronos.domain.repository

import com.kronos.domain.model.Quote
import com.kronos.domain.model.QuoteSummary
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getAllQuotes(): Flow<List<QuoteSummary>>
    fun getQuotesForBook(bookId: Long): Flow<List<Quote>>
    suspend fun addQuote(quote: Quote): Long
    suspend fun deleteQuote(id: Long)
}
