package com.kronos.feature.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.domain.model.Note
import com.kronos.domain.model.PdfPageBitmapState
import com.kronos.domain.model.Quote
import com.kronos.feature.reader.component.PdfPageView
import com.kronos.feature.reader.component.ReaderOverlay
import com.kronos.feature.reader.panel.BookmarkPanel
import com.kronos.feature.reader.panel.NotePanel
import com.kronos.feature.reader.panel.QuotePanel
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator

private enum class ReaderPanel { BOOKMARKS, NOTES, QUOTES }

@Composable
fun ReaderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pageStates by viewModel.pageStates.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ReaderUiState.Loading -> LoadingIndicator()
        is ReaderUiState.Error -> EmptyState(
            title = "Cannot open book",
            subtitle = state.message
        )
        is ReaderUiState.Success -> ReaderContent(
            state = state,
            pageStates = pageStates,
            notes = notes,
            quotes = quotes,
            onPageChanged = viewModel::onPageChanged,
            onToggleOverlay = viewModel::onToggleOverlay,
            onNavigateBack = onNavigateBack,
            onAddBookmark = viewModel::onAddBookmark,
            onDeleteBookmark = viewModel::onDeleteBookmark,
            onAddNote = viewModel::onAddNote,
            onUpdateNote = viewModel::onUpdateNote,
            onDeleteNote = viewModel::onDeleteNote,
            onAddQuote = viewModel::onAddQuote,
            onDeleteQuote = viewModel::onDeleteQuote
        )
    }
}

@Composable
private fun ReaderContent(
    state: ReaderUiState.Success,
    pageStates: Map<Int, PdfPageBitmapState>,
    notes: List<Note>,
    quotes: List<Quote>,
    onPageChanged: (Int) -> Unit,
    onToggleOverlay: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddBookmark: () -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onAddNote: (String, Int?) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddQuote: (String) -> Unit,
    onDeleteQuote: (Long) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = state.currentPage) { state.totalPages }
    var isZoomed by remember { mutableStateOf(false) }
    var openPanel by remember { mutableStateOf<ReaderPanel?>(null) }
    var jumpToPage by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
        isZoomed = false
    }

    LaunchedEffect(jumpToPage) {
        jumpToPage?.let { page ->
            pagerState.scrollToPage(page)
            jumpToPage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !isZoomed
        ) { pageIndex ->
            PdfPageView(
                state = pageStates[pageIndex] ?: PdfPageBitmapState.Idle,
                onIsZoomedChange = { isZoomed = it },
                onTap = { if (!isZoomed) onToggleOverlay() },
                modifier = Modifier.fillMaxSize()
            )
        }

        ReaderOverlay(
            book = state.book,
            currentPage = state.currentPage,
            totalPages = state.totalPages,
            isVisible = state.isOverlayVisible,
            onNavigateBack = onNavigateBack,
            onBookmarkClick = { openPanel = ReaderPanel.BOOKMARKS },
            onNoteClick = { openPanel = ReaderPanel.NOTES },
            onQuoteClick = { openPanel = ReaderPanel.QUOTES }
        )
    }

    when (openPanel) {
        ReaderPanel.BOOKMARKS -> BookmarkPanel(
            bookmarks = state.bookmarks,
            onDismiss = { openPanel = null },
            onAddBookmark = onAddBookmark,
            onNavigateToPage = { jumpToPage = it },
            onDeleteBookmark = onDeleteBookmark
        )
        ReaderPanel.NOTES -> NotePanel(
            notes = notes,
            currentPage = state.currentPage,
            onDismiss = { openPanel = null },
            onAddNote = onAddNote,
            onUpdateNote = onUpdateNote,
            onDeleteNote = onDeleteNote
        )
        ReaderPanel.QUOTES -> QuotePanel(
            quotes = quotes,
            currentPage = state.currentPage,
            onDismiss = { openPanel = null },
            onAddQuote = onAddQuote,
            onDeleteQuote = onDeleteQuote
        )
        null -> {}
    }
}
