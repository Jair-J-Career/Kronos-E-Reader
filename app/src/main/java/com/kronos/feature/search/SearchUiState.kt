package com.kronos.feature.search

import com.kronos.domain.model.Book

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Results(val query: String, val books: List<Book>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
