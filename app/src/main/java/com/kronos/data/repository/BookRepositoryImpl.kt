package com.kronos.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.kronos.common.IoDispatcher
import com.kronos.data.database.dao.BookDao
import com.kronos.data.database.entity.BookEntity
import com.kronos.domain.model.Book
import com.kronos.domain.repository.BookRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val dao: BookDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteBooks(): Flow<List<Book>> =
        dao.observeFavorites().map { list -> list.map { it.toDomain() } }

    override fun getTrashedBooks(): Flow<List<Book>> =
        dao.observeTrash().map { list -> list.map { it.toDomain() } }

    override fun searchBooks(query: String): Flow<List<Book>> =
        dao.searchByTitleOrAuthor(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getBookById(id: Long): Book? = withContext(ioDispatcher) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun addBook(book: Book): Long = withContext(ioDispatcher) {
        try {
            dao.insert(book.toEntity())
        } catch (e: SQLiteConstraintException) {
            -1L
        }
    }

    override suspend fun updateBook(book: Book) = withContext(ioDispatcher) {
        dao.update(book.toEntity())
    }

    override suspend fun moveToTrash(id: Long) = withContext(ioDispatcher) {
        dao.moveToTrash(id, System.currentTimeMillis())
    }

    override suspend fun restoreFromTrash(id: Long) = withContext(ioDispatcher) {
        dao.restoreFromTrash(id)
    }

    override suspend fun permanentlyDelete(id: Long) = withContext(ioDispatcher) {
        dao.deleteById(id)
    }

    override suspend fun toggleFavorite(id: Long) = withContext(ioDispatcher) {
        dao.toggleFavorite(id)
    }
}

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

private fun Book.toEntity() = BookEntity(
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
