package com.kronos.feature.library.info

import com.kronos.domain.model.Book
import com.kronos.domain.model.ReadingProgress

sealed class BookInfoUiState {
    object Loading : BookInfoUiState()
    data class Success(
        val book: Book,
        val progress: ReadingProgress?
    ) : BookInfoUiState()
    data class Error(val message: String) : BookInfoUiState()
}
