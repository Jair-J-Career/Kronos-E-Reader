package com.kronos.domain.usecase.book

import com.kronos.domain.model.Book
import com.kronos.domain.model.ReadingProgress
import com.kronos.domain.model.ReadingStatus
import com.kronos.domain.repository.BookRepository
import com.kronos.domain.repository.ReadingProgressRepository
import javax.inject.Inject

class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val progressRepository: ReadingProgressRepository
) {
    suspend operator fun invoke(book: Book): Long {
        val id = bookRepository.addBook(book)
        if (id > 0) {
            progressRepository.upsert(
                ReadingProgress(
                    bookId = id,
                    status = ReadingStatus.READING,
                    currentPage = 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return id
    }
}
