package com.kronos.feature.library.collections

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.feature.library.component.BookQuickItem
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    onNavigateBack: () -> Unit,
    onBookClick: (Long) -> Unit,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    val name = (uiState as? CollectionDetailUiState.Success)?.collectionName ?: ""
                    Text(name)
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CollectionDetailUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(paddingValues))
            is CollectionDetailUiState.Error -> EmptyState(
                title = "Something went wrong",
                subtitle = state.message,
                modifier = Modifier.padding(paddingValues),
                action = { TextButton(onClick = onNavigateBack) { Text("Go Back") } }
            )
            is CollectionDetailUiState.Success -> {
                if (state.books.isEmpty()) {
                    EmptyState(
                        title = "Empty collection",
                        subtitle = "Add books from their info pages",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        itemsIndexed(state.books, key = { _, book -> book.id }) { index, book ->
                            BookQuickItem(
                                book = book,
                                onClick = { onBookClick(book.id) }
                            )
                            if (index < state.books.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
