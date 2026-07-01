package com.kronos.data.di

import android.content.Context
import androidx.room.Room
import com.kronos.common.IoDispatcher
import com.kronos.data.database.DatabaseMigrations
import com.kronos.data.database.KronosDatabase
import com.kronos.data.database.dao.AuthorDao
import com.kronos.data.database.dao.BookDao
import com.kronos.data.database.dao.BookmarkDao
import com.kronos.data.database.dao.CollectionDao
import com.kronos.data.database.dao.NoteDao
import com.kronos.data.database.dao.QuoteDao
import com.kronos.data.database.dao.ReadingProgressDao
import com.kronos.data.database.dao.SearchHistoryDao
import com.kronos.data.database.dao.SeriesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKronosDatabase(@ApplicationContext context: Context): KronosDatabase =
        Room.databaseBuilder(context, KronosDatabase::class.java, "kronos.db")
            .addMigrations(DatabaseMigrations.MIGRATION_1_2, DatabaseMigrations.MIGRATION_2_3)
            .build()

    @Provides @Singleton fun provideBookDao(db: KronosDatabase): BookDao = db.bookDao()
    @Provides @Singleton fun provideAuthorDao(db: KronosDatabase): AuthorDao = db.authorDao()
    @Provides @Singleton fun provideSeriesDao(db: KronosDatabase): SeriesDao = db.seriesDao()
    @Provides @Singleton fun provideCollectionDao(db: KronosDatabase): CollectionDao = db.collectionDao()
    @Provides @Singleton fun provideReadingProgressDao(db: KronosDatabase): ReadingProgressDao = db.readingProgressDao()
    @Provides @Singleton fun provideBookmarkDao(db: KronosDatabase): BookmarkDao = db.bookmarkDao()
    @Provides @Singleton fun provideQuoteDao(db: KronosDatabase): QuoteDao = db.quoteDao()
    @Provides @Singleton fun provideNoteDao(db: KronosDatabase): NoteDao = db.noteDao()
    @Provides @Singleton fun provideSearchHistoryDao(db: KronosDatabase): SearchHistoryDao = db.searchHistoryDao()

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
