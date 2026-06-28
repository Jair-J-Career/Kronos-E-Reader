package com.kronos.domain.repository

import com.kronos.domain.model.Book
import com.kronos.domain.model.Collection
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getAllCollections(): Flow<List<Collection>>
    fun getBooksInCollection(collectionId: Long): Flow<List<Book>>
    fun getCollectionIdsForBook(bookId: Long): Flow<List<Long>>
    suspend fun getById(collectionId: Long): Collection?
    suspend fun createCollection(name: String): Long
    suspend fun addBookToCollection(bookId: Long, collectionId: Long)
    suspend fun removeBookFromCollection(bookId: Long, collectionId: Long)
}
