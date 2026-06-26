package com.kronos.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.domain.model.SearchHistoryItem
import com.kronos.domain.usecase.search.ClearSearchHistoryUseCase
import com.kronos.domain.usecase.search.GetSearchHistoryUseCase
import com.kronos.domain.usecase.search.SaveSearchQueryUseCase
import com.kronos.domain.usecase.search.SearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val saveSearchQueryUseCase: SaveSearchQueryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<SearchHistoryItem>> = getSearchHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .collectLatest { q ->
                    if (q.isBlank()) {
                        _uiState.value = SearchUiState.Idle
                        return@collectLatest
                    }
                    _uiState.value = SearchUiState.Loading
                    searchBooksUseCase(q).collect { books ->
                        _uiState.value = SearchUiState.Results(q, books)
                    }
                }
        }
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun onSearchSubmit(q: String) {
        if (q.isBlank()) return
        val resultCount = (_uiState.value as? SearchUiState.Results)?.books?.size
        viewModelScope.launch { saveSearchQueryUseCase(q, resultCount) }
    }

    fun onHistoryItemClick(query: String) {
        _query.value = query
    }

    fun onClearHistory() {
        viewModelScope.launch { clearSearchHistoryUseCase() }
    }
}
