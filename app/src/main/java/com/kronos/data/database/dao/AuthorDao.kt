package com.kronos.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kronos.data.database.entity.AuthorEntity
import com.kronos.data.database.entity.BookAuthorCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {

    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun observeAll(): Flow<List<AuthorEntity>>

    @Query("""
        SELECT a.* FROM authors a
        INNER JOIN book_author_cross_ref ref ON a.id = ref.author_id
        WHERE ref.book_id = :bookId
        ORDER BY a.name ASC
    """)
    fun observeByBook(bookId: Long): Flow<List<AuthorEntity>>

    @Query("SELECT * FROM authors WHERE id = :id")
    suspend fun getById(id: Long): AuthorEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(author: AuthorEntity): Long

    @Update
    suspend fun update(author: AuthorEntity)

    @Delete
    suspend fun delete(author: AuthorEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: BookAuthorCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: BookAuthorCrossRef)
}
