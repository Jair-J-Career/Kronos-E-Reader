package com.kronos.domain.usecase.search

import com.kronos.domain.model.Book
import com.kronos.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(query: String): Flow<List<Book>> =
        bookRepository.searchBooks("%${query.trim()}%")
}
