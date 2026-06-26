package com.kronos.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kronos.data.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE book_id = :bookId ORDER BY page_number ASC, created_at DESC")
    fun observeAllForBook(bookId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE book_id = :bookId AND page_number = :pageNumber ORDER BY created_at DESC")
    fun observeByPage(bookId: Long, pageNumber: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE book_id = :bookId AND page_number IS NULL ORDER BY created_at DESC")
    fun observeBookLevelNotes(bookId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
