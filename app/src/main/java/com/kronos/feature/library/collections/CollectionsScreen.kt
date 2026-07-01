package com.kronos.feature.library.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onNavigateBack: () -> Unit,
    onCollectionClick: (Long) -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val bookCounts by viewModel.bookCounts.collectAsStateWithLifecycle()
    val collectionCovers by viewModel.collectionCovers.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showViewSubmenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Collections") },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = {
                                showOverflowMenu = false
                                showViewSubmenu = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View  ›") },
                                onClick = { showViewSubmenu = !showViewSubmenu }
                            )
                            if (showViewSubmenu) {
                                CollectionViewMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.displayName()) },
                                        leadingIcon = if (viewMode == mode) {
                                            { Icon(Icons.Default.Check, contentDescription = null) }
                                        } else null,
                                        onClick = {
                                            viewModel.onViewModeChange(mode)
                                            showOverflowMenu = false
                                            showViewSubmenu = false
                                        },
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New collection")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CollectionsUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(paddingValues))
            is CollectionsUiState.Error -> EmptyState(
                title = "Something went wrong",
                subtitle = state.message,
                modifier = Modifier.padding(paddingValues),
                action = { TextButton(onClick = onNavigateBack) { Text("Go Back") } }
            )
            is CollectionsUiState.Success -> {
                if (state.collections.isEmpty()) {
                    EmptyState(
                        title = "No collections",
                        subtitle = "Tap + to create your first collection",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                } else {
                    when (viewMode) {
                        CollectionViewMode.QUICK -> LazyColumn(
                            contentPadding = paddingValues,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.collections, key = { it.id }) { collection ->
                                val count = bookCounts[collection.id] ?: 0
                                ListItem(
                                    headlineContent = { Text(collection.name) },
                                    supportingContent = {
                                        Text("$count ${if (count == 1) "book" else "books"}")
                                    },
                                    modifier = Modifier.clickable { onCollectionClick(collection.id) }
                                )
                                HorizontalDivider()
                            }
                        }

                        CollectionViewMode.COVERS -> LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = paddingValues,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                        ) {
                            items(state.collections, key = { it.id }) { collection ->
                                val covers = collectionCovers[collection.id] ?: emptyList()
                                ElevatedCard(
                                    onClick = { onCollectionClick(collection.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (covers.size >= 4) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1.4f)
                                            ) {
                                                CollectionCoverGrid(
                                                    covers = covers,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Text(
                                                text = collection.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1.4f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = collection.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        CollectionViewMode.GRID -> LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = paddingValues,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            items(state.collections, key = { it.id }) { collection ->
                                val covers = collectionCovers[collection.id] ?: emptyList()
                                ElevatedCard(
                                    onClick = { onCollectionClick(collection.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (covers.size >= 4) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                            ) {
                                                CollectionCoverGrid(
                                                    covers = covers,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Text(
                                                text = collection.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = collection.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.onCreateCollection(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Collection") },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(nameInput) },
                enabled = nameInput.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CollectionCoverGrid(covers: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AsyncImage(
                model = covers[0],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            AsyncImage(
                model = covers[1],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AsyncImage(
                model = covers[2],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            AsyncImage(
                model = covers[3],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

private fun CollectionViewMode.displayName() = when (this) {
    CollectionViewMode.QUICK -> "Quick"
    CollectionViewMode.COVERS -> "Covers"
    CollectionViewMode.GRID -> "Grid"
}
