package com.kronos.domain.usecase.collection

import com.kronos.domain.repository.CollectionRepository
import javax.inject.Inject

class AddBookToCollectionUseCase @Inject constructor(private val repository: CollectionRepository) {
    suspend operator fun invoke(bookId: Long, collectionId: Long) =
        repository.addBookToCollection(bookId, collectionId)
}
