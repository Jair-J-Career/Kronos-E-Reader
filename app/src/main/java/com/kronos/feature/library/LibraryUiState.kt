package com.kronos.feature.library

import com.kronos.domain.model.Book
import com.kronos.domain.model.SortMode
import com.kronos.domain.model.ViewMode

sealed class LibraryUiState {
    object Loading : LibraryUiState()
    data class Success(
        val books: List<Book>,
        val viewMode: ViewMode,
        val sortMode: SortMode
    ) : LibraryUiState()
    data class Error(val message: String) : LibraryUiState()
}
