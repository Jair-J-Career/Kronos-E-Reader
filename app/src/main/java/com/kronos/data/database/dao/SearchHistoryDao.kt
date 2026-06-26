package com.kronos.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kronos.data.database.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY searched_at DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity): Long

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteByQuery(query: String)

    @androidx.room.Transaction
    suspend fun upsert(item: SearchHistoryEntity) {
        deleteByQuery(item.query)
        insert(item)
    }

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
