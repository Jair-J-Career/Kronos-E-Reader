package com.kronos.domain.usecase.collection

import com.kronos.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollectionIdsForBookUseCase @Inject constructor(private val repository: CollectionRepository) {
    operator fun invoke(bookId: Long): Flow<List<Long>> =
        repository.getCollectionIdsForBook(bookId)
}
