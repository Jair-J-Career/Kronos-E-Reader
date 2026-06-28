package com.kronos.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.kronos.common.IoDispatcher
import com.kronos.data.database.dao.BookDao
import com.kronos.data.database.entity.BookEntity
import com.kronos.data.database.entity.BookWithAnnotations
import com.kronos.domain.model.Book
import com.kronos.domain.model.BookAnnotationSummary
import com.kronos.domain.model.ReadingStatus
import com.kronos.domain.model.SortMode
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

    override fun getAllBooks(sort: SortMode): Flow<List<Book>> = when (sort) {
        SortMode.RECENT -> dao.observeAll()
        SortMode.NAME -> dao.observeAllByName()
        SortMode.SIZE -> dao.observeAllBySize()
    }.map { list -> list.map { it.toDomain() } }

    override fun getBooksByStatus(status: ReadingStatus, sort: SortMode): Flow<List<Book>> = when (sort) {
        SortMode.RECENT -> dao.observeByStatusRecent(status.name)
        SortMode.NAME -> dao.observeByStatusName(status.name)
        SortMode.SIZE -> dao.observeByStatusSize(status.name)
    }.map { list -> list.map { it.toDomain() } }

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

    override suspend fun deleteAllTrashed() = withContext(ioDispatcher) {
        dao.deleteAllTrashed()
    }

    override suspend fun toggleFavorite(id: Long) = withContext(ioDispatcher) {
        dao.toggleFavorite(id)
    }

    override fun observeBooksWithAnnotations(): Flow<List<BookAnnotationSummary>> =
        dao.observeBooksWithAnnotations().map { list -> list.map { it.toSummary() } }
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

private fun BookWithAnnotations.toSummary(): BookAnnotationSummary {
    val latestQuote = quotes.maxOfOrNull { it.createdAt } ?: 0L
    val latestNote = notes.maxOfOrNull { it.createdAt } ?: 0L
    return BookAnnotationSummary(
        bookId = book.id,
        title = book.title,
        coverImagePath = book.coverImagePath,
        quoteCount = quotes.size,
        noteCount = notes.size,
        latestAnnotationAt = maxOf(latestQuote, latestNote)
    )
}

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
