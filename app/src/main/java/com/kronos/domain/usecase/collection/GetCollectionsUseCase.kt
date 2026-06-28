package com.kronos.domain.usecase.collection

import com.kronos.domain.model.Collection
import com.kronos.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollectionsUseCase @Inject constructor(private val repository: CollectionRepository) {
    operator fun invoke(): Flow<List<Collection>> = repository.getAllCollections()
}
