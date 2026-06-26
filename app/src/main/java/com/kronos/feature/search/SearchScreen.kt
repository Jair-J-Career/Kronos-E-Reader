package com.kronos.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.feature.search.component.SearchBar
import com.kronos.feature.search.component.SearchHistoryList
import com.kronos.feature.search.component.SearchResultItem
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBookClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::onSearchSubmit,
                modifier = Modifier.fillMaxWidth()
            )
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    SearchHistoryList(
                        history = history,
                        onItemClick = viewModel::onHistoryItemClick,
                        onClearHistory = viewModel::onClearHistory,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is SearchUiState.Loading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }
                is SearchUiState.Results -> {
                    if (state.books.isEmpty()) {
                        EmptyState(
                            title = "No results",
                            subtitle = "No books match \"${state.query}\"",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.books, key = { it.id }) { book ->
                                SearchResultItem(
                                    book = book,
                                    onClick = { onBookClick(book.id) }
                                )
                            }
                        }
                    }
                }
                is SearchUiState.Error -> {
                    EmptyState(
                        title = "Something went wrong",
                        subtitle = state.message,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
