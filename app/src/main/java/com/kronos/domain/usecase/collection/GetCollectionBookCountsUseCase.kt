package com.kronos.domain.usecase.collection

import com.kronos.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollectionBookCountsUseCase @Inject constructor(private val repository: CollectionRepository) {
    operator fun invoke(): Flow<Map<Long, Int>> = repository.observeBookCountsPerCollection()
}
