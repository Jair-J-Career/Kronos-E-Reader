package com.kronos.domain.repository

import com.kronos.domain.model.ReadingProgress

interface ReadingProgressRepository {
    suspend fun getByBookId(bookId: Long): ReadingProgress?
    suspend fun upsert(progress: ReadingProgress)
}
