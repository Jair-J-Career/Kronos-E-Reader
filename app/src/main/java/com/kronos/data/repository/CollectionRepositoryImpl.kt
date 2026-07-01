package com.kronos.data.repository

import com.kronos.common.IoDispatcher
import com.kronos.data.database.dao.CollectionCountRow
import com.kronos.data.database.dao.CollectionCoverRow
import com.kronos.data.database.dao.CollectionDao
import com.kronos.data.database.entity.BookCollectionCrossRef
import com.kronos.data.database.entity.BookEntity
import com.kronos.data.database.entity.CollectionEntity
import com.kronos.domain.model.Book
import com.kronos.domain.model.Collection
import com.kronos.domain.repository.CollectionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val dao: CollectionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CollectionRepository {

    override fun getAllCollections(): Flow<List<Collection>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun getBooksInCollection(collectionId: Long): Flow<List<Book>> =
        dao.observeBooksInCollection(collectionId).map { list -> list.map { it.toDomain() } }

    override fun getCollectionIdsForBook(bookId: Long): Flow<List<Long>> =
        dao.observeCollectionIdsForBook(bookId)

    override suspend fun getById(collectionId: Long): Collection? = withContext(ioDispatcher) {
        dao.getById(collectionId)?.toDomain()
    }

    override suspend fun createCollection(name: String): Long = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        dao.insert(CollectionEntity(name = name, createdAt = now, updatedAt = now))
    }

    override suspend fun addBookToCollection(bookId: Long, collectionId: Long) = withContext(ioDispatcher) {
        dao.insertCrossRef(BookCollectionCrossRef(bookId, collectionId, System.currentTimeMillis()))
    }

    override suspend fun removeBookFromCollection(bookId: Long, collectionId: Long) = withContext(ioDispatcher) {
        dao.deleteCrossRef(BookCollectionCrossRef(bookId, collectionId, 0L))
    }

    override fun observeBookCountsPerCollection(): Flow<Map<Long, Int>> =
        dao.observeBookCountsPerCollection().map { rows -> rows.associate { it.collectionId to it.count } }

    override fun observeCollectionCovers(): Flow<Map<Long, List<String>>> =
        dao.observeCollectionCovers().map { rows ->
            rows.groupBy { it.collectionId }
                .mapValues { (_, v) -> v.map { it.coverImagePath }.take(4) }
        }
}

private fun CollectionEntity.toDomain() = Collection(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sortOrder = sortOrder
)

private fun BookEntity.toDomain() = Book(
    id = id,
    title = title,
    filePath = filePath,
    fileUri = fileUri,
    fileSizeBytes = fileSizeBytes,
    pageCount = pageCount,
    coverImagePath = coverImagePath,
    addedAt = addedAt,
    lastOpenedAt = lastOpenedAt,
    isFavorite = isFavorite,
    isInTrash = isInTrash,
    trashedAt = trashedAt,
    seriesId = seriesId,
    seriesPosition = seriesPosition,
    embeddingId = embeddingId,
    sourceTextHash = sourceTextHash
)
