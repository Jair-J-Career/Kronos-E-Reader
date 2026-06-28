package com.kronos.domain.repository

import com.kronos.domain.model.Book
import com.kronos.domain.model.BookAnnotationSummary
import com.kronos.domain.model.ReadingStatus
import com.kronos.domain.model.SortMode
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(sort: SortMode): Flow<List<Book>>
    fun observeBooksWithAnnotations(): Flow<List<BookAnnotationSummary>>
    fun getBooksByStatus(status: ReadingStatus, sort: SortMode): Flow<List<Book>>
    fun getFavoriteBooks(): Flow<List<Book>>
    fun getTrashedBooks(): Flow<List<Book>>
    fun searchBooks(query: String): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun addBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun moveToTrash(id: Long)
    suspend fun restoreFromTrash(id: Long)
    suspend fun permanentlyDelete(id: Long)
    suspend fun deleteAllTrashed()
    suspend fun toggleFavorite(id: Long)
}
