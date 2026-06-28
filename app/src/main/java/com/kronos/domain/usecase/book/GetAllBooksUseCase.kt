package com.kronos.domain.usecase.book

import com.kronos.domain.model.Book
import com.kronos.domain.model.SortMode
import com.kronos.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllBooksUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(sort: SortMode = SortMode.RECENT): Flow<List<Book>> =
        repository.getAllBooks(sort)
}
