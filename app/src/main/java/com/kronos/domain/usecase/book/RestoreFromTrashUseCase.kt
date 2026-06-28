package com.kronos.domain.usecase.book

import com.kronos.domain.repository.BookRepository
import javax.inject.Inject

class RestoreFromTrashUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(bookId: Long) = repository.restoreFromTrash(bookId)
}
