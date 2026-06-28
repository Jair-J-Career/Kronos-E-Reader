package com.kronos.domain.usecase.readingprogress

import com.kronos.domain.model.ReadingProgress
import com.kronos.domain.repository.ReadingProgressRepository
import javax.inject.Inject

class UpsertReadingProgressUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    suspend operator fun invoke(progress: ReadingProgress) =
        repository.upsert(progress)
}
