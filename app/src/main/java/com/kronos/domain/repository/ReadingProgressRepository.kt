package com.kronos.domain.repository

import com.kronos.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

interface ReadingProgressRepository {
    fun observeByBookId(bookId: Long): Flow<ReadingProgress?>
    fun observeAll(): Flow<List<ReadingProgress>>
    suspend fun getByBookId(bookId: Long): ReadingProgress?
    suspend fun upsert(progress: ReadingProgress)
}
