package com.kronos.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.kronos.data.database.entity.BookEntity
import com.kronos.data.database.entity.BookWithAnnotations
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Transaction
    @Query("""
        SELECT * FROM books
        WHERE is_in_trash = 0
        AND id IN (SELECT book_id FROM quotes UNION SELECT book_id FROM notes)
    """)
    fun observeBooksWithAnnotations(): Flow<List<BookWithAnnotations>>

    @Query("""
        SELECT b.* FROM books b
        LEFT JOIN reading_progress rp ON b.id = rp.book_id
        WHERE b.is_in_trash = 0
        ORDER BY COALESCE(rp.updated_at, b.added_at) DESC
    """)
    fun observeAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_in_trash = 0 ORDER BY title ASC")
    fun observeAllByName(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_in_trash = 0 ORDER BY file_size_bytes DESC")
    fun observeAllBySize(): Flow<List<BookEntity>>

    @Query("""
        SELECT b.* FROM books b
        LEFT JOIN reading_progress rp ON b.id = rp.book_id
        WHERE b.is_in_trash = 0
        AND COALESCE(rp.status, 'TO_READ') = :status
        ORDER BY COALESCE(rp.updated_at, b.added_at) DESC
    """)
    fun observeByStatusRecent(status: String): Flow<List<BookEntity>>

    @Query("""
        SELECT b.* FROM books b
        LEFT JOIN reading_progress rp ON b.id = rp.book_id
        WHERE b.is_in_trash = 0
        AND COALESCE(rp.status, 'TO_READ') = :status
        ORDER BY b.title ASC
    """)
    fun observeByStatusName(status: String): Flow<List<BookEntity>>

    @Query("""
        SELECT b.* FROM books b
        LEFT JOIN reading_progress rp ON b.id = rp.book_id
        WHERE b.is_in_trash = 0
        AND COALESCE(rp.status, 'TO_READ') = :status
        ORDER BY b.file_size_bytes DESC
    """)
    fun observeByStatusSize(status: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_favorite = 1 AND is_in_trash = 0 ORDER BY title ASC")
    fun observeFavorites(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_in_trash = 1 ORDER BY trashed_at DESC")
    fun observeTrash(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE series_id = :seriesId AND is_in_trash = 0 ORDER BY series_position ASC")
    fun observeBySeries(seriesId: Long): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_in_trash = 0 AND title LIKE :query ORDER BY title ASC")
    fun search(query: String): Flow<List<BookEntity>>

    @Query("""
        SELECT DISTINCT b.* FROM books b
        LEFT JOIN book_author_cross_ref bac ON b.id = bac.book_id
        LEFT JOIN authors a ON bac.author_id = a.id
        WHERE b.is_in_trash = 0
        AND (b.title LIKE :query OR a.name LIKE :query)
        ORDER BY b.title ASC
    """)
    fun searchByTitleOrAuthor(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Query("UPDATE books SET is_in_trash = 1, trashed_at = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: Long, timestamp: Long)

    @Query("UPDATE books SET is_in_trash = 0, trashed_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM books WHERE is_in_trash = 1")
    suspend fun deleteAllTrashed()

    @Query("UPDATE books SET is_favorite = CASE WHEN is_favorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}
