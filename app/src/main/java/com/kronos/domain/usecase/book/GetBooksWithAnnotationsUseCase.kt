package com.kronos.domain.usecase.book

import com.kronos.domain.model.BookAnnotationSummary
import com.kronos.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBooksWithAnnotationsUseCase @Inject constructor(private val repository: BookRepository) {
    operator fun invoke(): Flow<List<BookAnnotationSummary>> = repository.observeBooksWithAnnotations()
}
