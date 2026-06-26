package com.kronos.domain.usecase.book

import com.kronos.domain.model.Book
import com.kronos.domain.repository.BookRepository
import javax.inject.Inject

class GetBookByIdUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(id: Long): Book? = repository.getBookById(id)
}
