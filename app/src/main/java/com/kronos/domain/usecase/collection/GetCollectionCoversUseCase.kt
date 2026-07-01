package com.kronos.domain.usecase.collection

import com.kronos.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollectionCoversUseCase @Inject constructor(private val repository: CollectionRepository) {
    operator fun invoke(): Flow<Map<Long, List<String>>> = repository.observeCollectionCovers()
}
