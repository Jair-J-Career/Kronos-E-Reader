package com.kronos.domain.usecase.collection

import com.kronos.domain.model.Book
import com.kronos.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBooksInCollectionUseCase @Inject constructor(private val repository: CollectionRepository) {
    operator fun invoke(collectionId: Long): Flow<List<Book>> =
        repository.getBooksInCollection(collectionId)
}
