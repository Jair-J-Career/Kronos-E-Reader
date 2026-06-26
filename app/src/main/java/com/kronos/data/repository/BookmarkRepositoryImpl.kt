package com.kronos.data.repository

import com.kronos.data.database.dao.BookmarkDao
import com.kronos.data.database.entity.BookmarkEntity
import com.kronos.domain.model.Bookmark
import com.kronos.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val dao: BookmarkDao
) : BookmarkRepository {

    override fun getBookmarksForBook(bookId: Long): Flow<List<Bookmark>> =
        dao.observeByBookId(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addBookmark(bookmark: Bookmark): Long = dao.insert(bookmark.toEntity())

    override suspend fun deleteBookmark(id: Long) = dao.deleteById(id)

    private fun BookmarkEntity.toDomain() = Bookmark(
        id = id,
        bookId = bookId,
        pageNumber = pageNumber,
        label = label,
        createdAt = createdAt,
        embeddingId = embeddingId,
        sourceTextHash = sourceTextHash
    )

    private fun Bookmark.toEntity() = BookmarkEntity(
        id = id,
        bookId = bookId,
        pageNumber = pageNumber,
        label = label,
        createdAt = createdAt,
        embeddingId = embeddingId,
        sourceTextHash = sourceTextHash
    )
}
