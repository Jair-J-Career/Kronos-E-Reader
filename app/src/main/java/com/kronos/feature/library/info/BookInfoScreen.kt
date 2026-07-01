package com.kronos.feature.library.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kronos.common.formatFileSize
import com.kronos.domain.model.Book
import com.kronos.domain.model.Collection
import com.kronos.domain.model.ReadingProgress
import com.kronos.domain.model.ReadingStatus
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfoScreen(
    onNavigateBack: () -> Unit,
    onCoverClick: (Long) -> Unit = {},
    viewModel: BookInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                BookInfoEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is BookInfoUiState.Loading -> LoadingIndicator()
            is BookInfoUiState.Error -> EmptyState(
                title = "Error",
                subtitle = state.message,
                action = { TextButton(onClick = onNavigateBack) { Text("Go Back") } }
            )
            is BookInfoUiState.Success -> BookInfoContent(
                state = state,
                onNavigateBack = onNavigateBack,
                onCoverClick = { onCoverClick(state.book.id) },
                onStatusChange = viewModel::onStatusChange,
                onCollectionToggle = viewModel::onCollectionToggle,
                onCreateCollection = viewModel::onCreateAndAddCollection,
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(state.book.fileUri))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                onMoveToTrash = viewModel::onMoveToTrash,
                onSaveReview = viewModel::onSaveReview
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookInfoContent(
    state: BookInfoUiState.Success,
    onNavigateBack: () -> Unit,
    onCoverClick: () -> Unit,
    onStatusChange: (ReadingStatus) -> Unit,
    onCollectionToggle: (Long, Boolean) -> Unit,
    onCreateCollection: (String) -> Unit,
    onShare: () -> Unit,
    onMoveToTrash: () -> Unit,
    onSaveReview: (Int?, String?) -> Unit
) {
    var showCollectionsSheet by remember { mutableStateOf(false) }
    var showReviewSheet by remember { mutableStateOf(false) }
    val collectionsSheetState = rememberModalBottomSheetState()
    val reviewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentStatus = state.progress?.status ?: ReadingStatus.READING
    val hasReview = state.progress?.rating != null || !state.progress?.review.isNullOrBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = state.book.title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showReviewSheet = true }) {
                Icon(
                    imageVector = if (hasReview) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Rate & Review"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .aspectRatio(0.707f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onCoverClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.book.coverImagePath != null) {
                        AsyncImage(
                            model = state.book.coverImagePath,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = state.book.title.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { onStatusChange(ReadingStatus.TO_READ) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Mark as To Do",
                            tint = if (currentStatus == ReadingStatus.TO_READ)
                                MaterialTheme.colorScheme.primary
                            else
                                LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { onStatusChange(ReadingStatus.HAVE_READ) }) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Mark as Completed",
                            tint = if (currentStatus == ReadingStatus.HAVE_READ)
                                MaterialTheme.colorScheme.primary
                            else
                                LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { showCollectionsSheet = true }) {
                        Icon(Icons.Default.Collections, contentDescription = "Collections")
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onMoveToTrash) {
                        Icon(Icons.Default.Delete, contentDescription = "Move to trash")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            MetadataCard {
                MetadataRow(
                    label = "Current Progress",
                    value = "${state.progress?.readPercentage?.toInt() ?: 0}%"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                MetadataRow(
                    label = "Status",
                    value = currentStatus.label()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                MetadataRow(
                    label = "Last Read",
                    value = if (state.progress != null) formatEpoch(state.progress.updatedAt) else "Never"
                )
            }

            if (hasReview) {
                Spacer(Modifier.height(12.dp))
                MetadataCard {
                    if (state.progress?.rating != null) {
                        MetadataRow(label = "Rating", value = "${state.progress.rating} / 10")
                    }
                    if (!state.progress?.review.isNullOrBlank()) {
                        if (state.progress?.rating != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                        MetadataRow(label = "Review", value = state.progress?.review ?: "")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            MetadataCard {
                MetadataRow(
                    label = "File Format",
                    value = state.book.filePath.substringAfterLast('.', "pdf").uppercase()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                MetadataRow(
                    label = "File Size",
                    value = formatFileSize(state.book.fileSizeBytes)
                )
            }

            Spacer(Modifier.height(12.dp))

            MetadataCard {
                MetadataRow(label = "File Path", value = state.book.filePath)
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showCollectionsSheet) {
        CollectionsBottomSheet(
            collections = state.collections,
            bookCollectionIds = state.bookCollectionIds,
            sheetState = collectionsSheetState,
            onDismiss = { showCollectionsSheet = false },
            onToggle = onCollectionToggle,
            onCreateCollection = onCreateCollection
        )
    }

    if (showReviewSheet) {
        ReviewBottomSheet(
            currentRating = state.progress?.rating,
            currentReview = state.progress?.review,
            sheetState = reviewSheetState,
            onDismiss = { showReviewSheet = false },
            onSave = { rating, review ->
                onSaveReview(rating, review)
                showReviewSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewBottomSheet(
    currentRating: Int?,
    currentReview: String?,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onSave: (Int?, String?) -> Unit
) {
    var selectedRating by remember { mutableStateOf(currentRating) }
    var reviewText by remember { mutableStateOf(currentReview ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Rate & Review",
                style = MaterialTheme.typography.titleMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (selectedRating != null) "Rating: $selectedRating / 10" else "Rating",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RatingSelector(
                    selected = selectedRating,
                    onSelect = { n -> selectedRating = if (selectedRating == n) null else n }
                )
            }

            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Your review") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onSave(selectedRating, reviewText.takeIf { it.isNotBlank() }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun RatingSelector(selected: Int?, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 1..10) {
            val filled = i <= (selected ?: 0)
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (filled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onSelect(i) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionsBottomSheet(
    collections: List<Collection>,
    bookCollectionIds: Set<Long>,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onToggle: (Long, Boolean) -> Unit,
    onCreateCollection: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add to Collection",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { showCreateDialog = true }) {
                Text("+ New")
            }
        }
        if (collections.isEmpty()) {
            Text(
                text = "No collections yet. Tap + New to create one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(bottom = 24.dp)) {
                items(collections, key = { it.id }) { collection ->
                    val checked = collection.id in bookCollectionIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(collection.id, !checked) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onToggle(collection.id, it) }
                        )
                        Text(
                            text = collection.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                onCreateCollection(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateCollectionDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
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
private fun MetadataCard(content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            content()
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.62f)
        )
    }
}

private fun ReadingStatus.label() = when (this) {
    ReadingStatus.TO_READ -> "To Read"
    ReadingStatus.READING -> "Reading"
    ReadingStatus.HAVE_READ -> "Completed"
}

private fun formatEpoch(epochMs: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMs))
