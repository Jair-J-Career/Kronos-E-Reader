package com.kronos.feature.library

import com.kronos.domain.model.Book

sealed class LibraryUiState {
    object Loading : LibraryUiState()
    data class Success(val books: List<Book>) : LibraryUiState()
    data class Error(val message: String) : LibraryUiState()
}
