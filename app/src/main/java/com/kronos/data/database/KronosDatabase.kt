package com.kronos.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kronos.data.database.converter.RoomTypeConverters
import com.kronos.data.database.dao.AuthorDao
import com.kronos.data.database.dao.BookDao
import com.kronos.data.database.dao.BookmarkDao
import com.kronos.data.database.dao.CollectionDao
import com.kronos.data.database.dao.NoteDao
import com.kronos.data.database.dao.QuoteDao
import com.kronos.data.database.dao.ReadingProgressDao
import com.kronos.data.database.dao.SearchHistoryDao
import com.kronos.data.database.dao.SeriesDao
import com.kronos.data.database.entity.AuthorEntity
import com.kronos.data.database.entity.BookAuthorCrossRef
import com.kronos.data.database.entity.BookCollectionCrossRef
import com.kronos.data.database.entity.BookEntity
import com.kronos.data.database.entity.BookmarkEntity
import com.kronos.data.database.entity.CollectionEntity
import com.kronos.data.database.entity.NoteEntity
import com.kronos.data.database.entity.QuoteEntity
import com.kronos.data.database.entity.ReadingProgressEntity
import com.kronos.data.database.entity.SearchHistoryEntity
import com.kronos.data.database.entity.SeriesEntity

@Database(
    entities = [
        BookEntity::class,
        AuthorEntity::class,
        SeriesEntity::class,
        CollectionEntity::class,
        BookAuthorCrossRef::class,
        BookCollectionCrossRef::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        QuoteEntity::class,
        NoteEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class KronosDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
    abstract fun seriesDao(): SeriesDao
    abstract fun collectionDao(): CollectionDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun quoteDao(): QuoteDao
    abstract fun noteDao(): NoteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
