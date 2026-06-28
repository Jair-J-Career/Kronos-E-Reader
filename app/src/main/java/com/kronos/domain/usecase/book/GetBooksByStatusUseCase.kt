package com.kronos.domain.usecase.book

import com.kronos.domain.model.Book
import com.kronos.domain.model.ReadingStatus
import com.kronos.domain.model.SortMode
import com.kronos.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBooksByStatusUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(status: ReadingStatus, sort: SortMode = SortMode.RECENT): Flow<List<Book>> =
        repository.getBooksByStatus(status, sort)
}
