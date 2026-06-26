package com.kronos.data.repository

import com.kronos.data.database.dao.QuoteDao
import com.kronos.data.database.entity.QuoteEntity
import com.kronos.domain.model.Quote
import com.kronos.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val dao: QuoteDao
) : QuoteRepository {

    override fun getQuotesForBook(bookId: Long): Flow<List<Quote>> =
        dao.observeByBookId(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addQuote(quote: Quote): Long = dao.insert(quote.toEntity())

    override suspend fun deleteQuote(id: Long) = dao.deleteById(id)

    private fun QuoteEntity.toDomain() = Quote(
        id = id,
        bookId = bookId,
        pageNumber = pageNumber,
        text = text,
        createdAt = createdAt,
        updatedAt = updatedAt,
        embeddingId = embeddingId,
        sourceTextHash = sourceTextHash
    )

    private fun Quote.toEntity() = QuoteEntity(
        id = id,
        bookId = bookId,
        pageNumber = pageNumber,
        text = text,
        createdAt = createdAt,
        updatedAt = updatedAt,
        embeddingId = embeddingId,
        sourceTextHash = sourceTextHash
    )
}
