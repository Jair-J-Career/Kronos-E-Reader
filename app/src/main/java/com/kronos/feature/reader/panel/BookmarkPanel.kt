package com.kronos.feature.reader.panel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kronos.domain.model.Bookmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkPanel(
    bookmarks: List<Bookmark>,
    onDismiss: () -> Unit,
    onAddBookmark: () -> Unit,
    onNavigateToPage: (Int) -> Unit,
    onDeleteBookmark: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bookmarks", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { onAddBookmark(); onDismiss() }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add here")
                }
            }
            Spacer(Modifier.height(8.dp))
            if (bookmarks.isEmpty()) {
                Text(
                    text = "No bookmarks yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        ListItem(
                            headlineContent = {
                                Text(bookmark.label ?: "Page ${bookmark.pageNumber + 1}")
                            },
                            supportingContent = if (bookmark.label != null) {
                                { Text("Page ${bookmark.pageNumber + 1}") }
                            } else null,
                            trailingContent = {
                                IconButton(onClick = { onDeleteBookmark(bookmark.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            },
                            modifier = Modifier.clickable {
                                onNavigateToPage(bookmark.pageNumber)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
