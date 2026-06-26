package com.kronos.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kronos.data.database.entity.BookCollectionCrossRef
import com.kronos.data.database.entity.BookEntity
import com.kronos.data.database.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY sort_order ASC, name ASC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("""
        SELECT b.* FROM books b
        INNER JOIN book_collection_cross_ref ref ON b.id = ref.book_id
        WHERE ref.collection_id = :collectionId AND b.is_in_trash = 0
    """)
    fun observeBooksInCollection(collectionId: Long): Flow<List<BookEntity>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getById(id: Long): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(collection: CollectionEntity): Long

    @Update
    suspend fun update(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: BookCollectionCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: BookCollectionCrossRef)
}
