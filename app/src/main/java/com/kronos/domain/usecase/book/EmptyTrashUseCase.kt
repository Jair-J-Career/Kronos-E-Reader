package com.kronos.domain.usecase.book

import com.kronos.domain.repository.BookRepository
import javax.inject.Inject

class EmptyTrashUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke() = repository.deleteAllTrashed()
}
