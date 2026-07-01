package com.kronos.data.database.dao

import androidx.room.ColumnInfo
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

data class CollectionCountRow(
    @ColumnInfo(name = "collection_id") val collectionId: Long,
    @ColumnInfo(name = "count") val count: Int
)

data class CollectionCoverRow(
    @ColumnInfo(name = "collection_id") val collectionId: Long,
    @ColumnInfo(name = "cover_image_path") val coverImagePath: String
)

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

    @Query("SELECT collection_id FROM book_collection_cross_ref WHERE book_id = :bookId")
    fun observeCollectionIdsForBook(bookId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: BookCollectionCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: BookCollectionCrossRef)

    @Query("SELECT collection_id, COUNT(*) AS count FROM book_collection_cross_ref GROUP BY collection_id")
    fun observeBookCountsPerCollection(): Flow<List<CollectionCountRow>>

    @Query("""
        SELECT ref.collection_id AS collection_id, b.cover_image_path AS cover_image_path
        FROM book_collection_cross_ref ref
        INNER JOIN books b ON ref.book_id = b.id
        WHERE b.is_in_trash = 0 AND b.cover_image_path IS NOT NULL
        ORDER BY ref.added_at DESC
    """)
    fun observeCollectionCovers(): Flow<List<CollectionCoverRow>>
}
