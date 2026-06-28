package com.kronos.data.repository

import com.kronos.common.IoDispatcher
import com.kronos.data.database.dao.ReadingProgressDao
import com.kronos.data.database.entity.ReadingProgressEntity
import com.kronos.domain.model.ReadingProgress
import com.kronos.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingProgressRepositoryImpl @Inject constructor(
    private val dao: ReadingProgressDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReadingProgressRepository {

    override fun observeByBookId(bookId: Long): Flow<ReadingProgress?> =
        dao.observeByBookId(bookId).map { it?.toDomain() }

    override fun observeAll(): Flow<List<ReadingProgress>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getByBookId(bookId: Long): ReadingProgress? =
        withContext(ioDispatcher) { dao.getByBookId(bookId)?.toDomain() }

    override suspend fun upsert(progress: ReadingProgress) =
        withContext(ioDispatcher) {
            dao.upsert(progress.toEntity().copy(updatedAt = System.currentTimeMillis()))
        }
}

private fun ReadingProgressEntity.toDomain() = ReadingProgress(
    id = id,
    bookId = bookId,
    status = status,
    currentPage = currentPage,
    readPercentage = readPercentage,
    startedAt = startedAt,
    completedAt = completedAt,
    updatedAt = updatedAt,
    isNightMode = isNightMode
)

private fun ReadingProgress.toEntity() = ReadingProgressEntity(
    id = id,
    bookId = bookId,
    status = status,
    currentPage = currentPage,
    readPercentage = readPercentage,
    startedAt = startedAt,
    completedAt = completedAt,
    updatedAt = updatedAt,
    isNightMode = isNightMode
)
