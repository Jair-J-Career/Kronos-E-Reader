package com.kronos.feature.reader.progress

import com.kronos.domain.model.Book
import com.kronos.domain.model.Bookmark

sealed class ProgressUiState {
    object Loading : ProgressUiState()
    data class Success(
        val book: Book,
        val currentPage: Int,
        val totalPages: Int,
        val readPercentage: Float,
        val bookmarks: List<Bookmark>,
        val octantCounts: List<Int>,
        val maxAnnotations: Int
    ) : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}
