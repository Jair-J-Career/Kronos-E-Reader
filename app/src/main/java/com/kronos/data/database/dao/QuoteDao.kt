package com.kronos.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kronos.data.database.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("""
        SELECT q.id, q.book_id, q.text, q.created_at, b.title AS book_title
        FROM quotes q
        INNER JOIN books b ON q.book_id = b.id
        WHERE b.is_in_trash = 0
        ORDER BY q.created_at DESC
    """)
    fun observeAllWithBookTitle(): Flow<List<QuoteWithBookTitle>>

    @Query("SELECT * FROM quotes WHERE book_id = :bookId ORDER BY page_number ASC, created_at DESC")
    fun observeByBookId(bookId: Long): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getById(id: Long): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(quote: QuoteEntity): Long

    @Update
    suspend fun update(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
