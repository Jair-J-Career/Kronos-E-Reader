package com.kronos.domain.usecase.readingprogress

import com.kronos.domain.model.ReadingProgress
import com.kronos.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllReadingProgressUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    operator fun invoke(): Flow<List<ReadingProgress>> = repository.observeAll()
}
