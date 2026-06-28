package com.kronos.data.di

import com.kronos.data.repository.BookmarkRepositoryImpl
import com.kronos.data.repository.BookRepositoryImpl
import com.kronos.data.repository.NoteRepositoryImpl
import com.kronos.data.repository.QuoteRepositoryImpl
import com.kronos.data.repository.ReadingProgressRepositoryImpl
import com.kronos.data.repository.SearchHistoryRepositoryImpl
import com.kronos.data.repository.ThemeRepositoryImpl
import com.kronos.domain.repository.BookmarkRepository
import com.kronos.domain.repository.BookRepository
import com.kronos.domain.repository.NoteRepository
import com.kronos.domain.repository.QuoteRepository
import com.kronos.domain.repository.ReadingProgressRepository
import com.kronos.domain.repository.SearchHistoryRepository
import com.kronos.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds @Singleton
    abstract fun bindQuoteRepository(impl: QuoteRepositoryImpl): QuoteRepository

    @Binds @Singleton
    abstract fun bindReadingProgressRepository(impl: ReadingProgressRepositoryImpl): ReadingProgressRepository

    @Binds @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository

    @Binds @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
