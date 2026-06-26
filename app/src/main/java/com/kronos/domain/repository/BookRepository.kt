package com.kronos.domain.repository

import com.kronos.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getFavoriteBooks(): Flow<List<Book>>
    fun getTrashedBooks(): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun addBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun moveToTrash(id: Long)
    suspend fun restoreFromTrash(id: Long)
    suspend fun permanentlyDelete(id: Long)
    suspend fun toggleFavorite(id: Long)
}
