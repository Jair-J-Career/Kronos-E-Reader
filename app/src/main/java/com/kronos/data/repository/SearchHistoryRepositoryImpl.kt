package com.kronos.data.repository

import com.kronos.common.IoDispatcher
import com.kronos.data.database.dao.SearchHistoryDao
import com.kronos.data.database.entity.SearchHistoryEntity
import com.kronos.domain.model.SearchHistoryItem
import com.kronos.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val dao: SearchHistoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchHistoryRepository {

    override fun getRecentQueries(limit: Int): Flow<List<SearchHistoryItem>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun save(query: String, resultCount: Int?) = withContext(ioDispatcher) {
        dao.upsert(
            SearchHistoryEntity(
                query = query,
                searchedAt = System.currentTimeMillis(),
                resultCount = resultCount
            )
        )
    }

    override suspend fun deleteById(id: Long) = withContext(ioDispatcher) {
        dao.deleteById(id)
    }

    override suspend fun clearAll() = withContext(ioDispatcher) {
        dao.clearAll()
    }
}

private fun SearchHistoryEntity.toDomain() = SearchHistoryItem(
    id = id,
    query = query,
    searchedAt = searchedAt,
    resultCount = resultCount
)
