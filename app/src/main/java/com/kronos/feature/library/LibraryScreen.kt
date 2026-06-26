package com.kronos.feature.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.feature.library.component.BookGrid
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val safLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onTreeUriGranted(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LibraryEvent.OpenSafPicker -> safLauncher.launch(null)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Library") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onGrantFolderAccess) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add books")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is LibraryUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is LibraryUiState.Success -> {
                if (state.books.isEmpty()) {
                    EmptyState(
                        title = "Your library is empty",
                        subtitle = "Grant access to a folder to scan it for PDFs",
                        modifier = Modifier.padding(paddingValues),
                        action = {
                            Button(onClick = viewModel::onGrantFolderAccess) {
                                Text("Grant Folder Access")
                            }
                        }
                    )
                } else {
                    BookGrid(
                        books = state.books,
                        onBookClick = { book -> onBookClick(book.id) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
            is LibraryUiState.Error -> {
                EmptyState(
                    title = "Something went wrong",
                    subtitle = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
