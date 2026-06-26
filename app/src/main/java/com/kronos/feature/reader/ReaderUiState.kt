package com.kronos.feature.reader

import com.kronos.domain.model.Book
import com.kronos.domain.model.Bookmark
import com.kronos.domain.model.ReadingProgress

sealed class ReaderUiState {
    object Loading : ReaderUiState()
    data class Success(
        val book: Book,
        val totalPages: Int,
        val currentPage: Int,
        val readingProgress: ReadingProgress,
        val bookmarks: List<Bookmark>,
        val isOverlayVisible: Boolean
    ) : ReaderUiState()
    data class Error(val message: String) : ReaderUiState()
}
