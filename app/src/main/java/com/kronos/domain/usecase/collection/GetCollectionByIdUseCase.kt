package com.kronos.domain.usecase.collection

import com.kronos.domain.model.Collection
import com.kronos.domain.repository.CollectionRepository
import javax.inject.Inject

class GetCollectionByIdUseCase @Inject constructor(private val repository: CollectionRepository) {
    suspend operator fun invoke(id: Long): Collection? = repository.getById(id)
}
