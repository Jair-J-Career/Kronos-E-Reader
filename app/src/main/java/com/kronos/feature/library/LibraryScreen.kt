package com.kronos.feature.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.feature.library.component.BookListItem
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookCoverClick: (Long) -> Unit,
    onBookTitleClick: (Long) -> Unit,
    onBookProgressClick: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let { viewModel.onTreeUriGranted(it) } }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> if (uris.isNotEmpty()) viewModel.onFilesGranted(uris) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LibraryEvent.OpenFolderPicker -> folderPickerLauncher.launch(null)
                LibraryEvent.OpenFilePicker -> filePickerLauncher.launch(arrayOf("application/pdf"))
            }
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                        }
                    },
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Search books…") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
                )
            } else {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                    },
                    title = { Text("My Library") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        Box {
                            IconButton(onClick = { showOverflowMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open Single File") },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.onGrantFileAccess()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Open Folder") },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.onGrantFolderAccess()
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Sort By  ›") },
                                    onClick = { showOverflowMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("View  ›") },
                                    onClick = { showOverflowMenu = false }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Share App") },
                                    onClick = { showOverflowMenu = false }
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is LibraryUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is LibraryUiState.Success -> {
                when {
                    state.books.isEmpty() && searchQuery.isBlank() -> EmptyState(
                        title = "Your library is empty",
                        subtitle = "Add PDFs to get started",
                        modifier = Modifier.padding(paddingValues),
                        action = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = viewModel::onGrantFolderAccess) {
                                    Text("Scan Folder")
                                }
                                Button(onClick = viewModel::onGrantFileAccess) {
                                    Text("Add Files")
                                }
                            }
                        }
                    )
                    state.books.isEmpty() -> EmptyState(
                        title = "No results",
                        subtitle = "No books match \"$searchQuery\"",
                        modifier = Modifier.padding(paddingValues)
                    )
                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        itemsIndexed(state.books, key = { _, book -> book.id }) { index, book ->
                            BookListItem(
                                book = book,
                                readPercentage = 0f,
                                onCoverClick = { onBookCoverClick(book.id) },
                                onTitleClick = { onBookTitleClick(book.id) },
                                onProgressClick = { onBookProgressClick(book.id) }
                            )
                            if (index < state.books.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
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
