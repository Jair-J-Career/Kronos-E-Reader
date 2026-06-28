package com.kronos.feature.reader.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.Bookmark
import com.kronos.domain.model.Book
import com.kronos.domain.model.Note
import com.kronos.domain.model.Quote
import com.kronos.domain.usecase.book.GetBookByIdUseCase
import com.kronos.domain.usecase.bookmark.GetBookmarksForBookUseCase
import com.kronos.domain.usecase.note.GetNotesForBookUseCase
import com.kronos.domain.usecase.quote.GetQuotesForBookUseCase
import com.kronos.domain.usecase.readingprogress.GetReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getBookById: GetBookByIdUseCase,
    private val getReadingProgress: GetReadingProgressUseCase,
    private val getNotesForBook: GetNotesForBookUseCase,
    private val getQuotesForBook: GetQuotesForBookUseCase,
    private val getBookmarksForBook: GetBookmarksForBookUseCase,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(ioDispatcher) {
            val book = getBookById(bookId)
            if (book == null) {
                _uiState.value = ProgressUiState.Error("Book not found")
                return@launch
            }
            val progress = getReadingProgress(bookId)
            val totalPages = book.pageCount.coerceAtLeast(1)

            combine(
                getNotesForBook(bookId),
                getQuotesForBook(bookId),
                getBookmarksForBook(bookId)
            ) { notes, quotes, bookmarks ->
                buildState(
                    totalPages = totalPages,
                    currentPage = progress?.currentPage ?: 0,
                    readPercentage = (progress?.readPercentage ?: 0.0).toFloat(),
                    notes = notes,
                    quotes = quotes,
                    bookmarks = bookmarks,
                    bookItem = book
                )
            }.collect { _uiState.value = it }
        }
    }

    private fun buildState(
        totalPages: Int,
        currentPage: Int,
        readPercentage: Float,
        notes: List<Note>,
        quotes: List<Quote>,
        bookmarks: List<Bookmark>,
        bookItem: Book
    ): ProgressUiState.Success {
        val counts = IntArray(8)
        notes.forEach { note ->
            val page = note.pageNumber ?: return@forEach
            val bucket = ((page.toFloat() / totalPages) * 8).toInt().coerceIn(0, 7)
            counts[bucket]++
        }
        quotes.forEach { quote ->
            val bucket = ((quote.pageNumber.toFloat() / totalPages) * 8).toInt().coerceIn(0, 7)
            counts[bucket]++
        }
        return ProgressUiState.Success(
            book = bookItem,
            currentPage = currentPage,
            totalPages = totalPages,
            readPercentage = readPercentage,
            bookmarks = bookmarks,
            octantCounts = counts.toList(),
            maxAnnotations = counts.max().coerceAtLeast(1)
        )
    }
}
