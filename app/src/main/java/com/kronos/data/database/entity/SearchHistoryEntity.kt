package com.kronos.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    indices = [Index("query"), Index("searched_at")]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    @ColumnInfo(name = "searched_at") val searchedAt: Long,
    @ColumnInfo(name = "result_count") val resultCount: Int? = null
)
