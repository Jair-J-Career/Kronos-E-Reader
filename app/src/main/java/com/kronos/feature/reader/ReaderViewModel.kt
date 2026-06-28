package com.kronos.feature.reader

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.data.pdf.PdfDocumentSession
import com.kronos.data.pdf.PdfPageCache
import com.kronos.data.pdf.PdfRenderWorker
import com.kronos.domain.model.Bookmark
import com.kronos.domain.model.Note
import com.kronos.domain.model.PdfPageBitmapState
import com.kronos.domain.model.Quote
import com.kronos.domain.model.ReadingProgress
import com.kronos.domain.model.ReadingStatus
import com.kronos.domain.usecase.book.GetBookByIdUseCase
import com.kronos.domain.usecase.bookmark.AddBookmarkUseCase
import com.kronos.domain.usecase.bookmark.DeleteBookmarkUseCase
import com.kronos.domain.usecase.bookmark.GetBookmarksForBookUseCase
import com.kronos.domain.usecase.note.AddNoteUseCase
import com.kronos.domain.usecase.note.DeleteNoteUseCase
import com.kronos.domain.usecase.note.GetNotesForBookUseCase
import com.kronos.domain.usecase.note.UpdateNoteUseCase
import com.kronos.domain.usecase.quote.AddQuoteUseCase
import com.kronos.domain.usecase.quote.DeleteQuoteUseCase
import com.kronos.domain.usecase.quote.GetQuotesForBookUseCase
import com.kronos.domain.source.TextExtractionSource
import com.kronos.domain.usecase.readingprogress.GetReadingProgressUseCase
import com.kronos.domain.usecase.readingprogress.UpsertReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val session: PdfDocumentSession,
    private val getBookById: GetBookByIdUseCase,
    private val getReadingProgress: GetReadingProgressUseCase,
    private val upsertReadingProgress: UpsertReadingProgressUseCase,
    private val getBookmarksForBook: GetBookmarksForBookUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val getNotesForBook: GetNotesForBookUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getQuotesForBook: GetQuotesForBookUseCase,
    private val addQuoteUseCase: AddQuoteUseCase,
    private val deleteQuoteUseCase: DeleteQuoteUseCase,
    private val textExtractionSource: TextExtractionSource,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _pageStates = MutableStateFlow<Map<Int, PdfPageBitmapState>>(emptyMap())
    val pageStates: StateFlow<Map<Int, PdfPageBitmapState>> = _pageStates.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _pageText = MutableStateFlow<String?>(null)
    val pageText: StateFlow<String?> = _pageText.asStateFlow()

    private val currentPageFlow = MutableStateFlow(0)
    private val cache = PdfPageCache()
    private val renderWidth = appContext.resources.displayMetrics.widthPixels
    private val worker = PdfRenderWorker(
        session = session,
        cache = cache,
        pageStates = _pageStates,
        renderWidth = renderWidth,
        ioDispatcher = ioDispatcher
    )

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val book = getBookById(bookId) ?: run {
                    _uiState.value = ReaderUiState.Error("Book not found")
                    return@launch
                }
                session.open(book.fileUri)
                val totalPages = session.pageCount
                if (totalPages <= 0) {
                    _uiState.value = ReaderUiState.Error("Could not read PDF pages")
                    return@launch
                }
                val progress = getReadingProgress(bookId)
                    ?: ReadingProgress(bookId = bookId, status = ReadingStatus.READING)
                currentPageFlow.value = progress.currentPage
                _uiState.value = ReaderUiState.Success(
                    book = book,
                    totalPages = totalPages,
                    currentPage = progress.currentPage,
                    readingProgress = progress,
                    bookmarks = emptyList(),
                    isOverlayVisible = false
                )
                worker.start(currentPageFlow, viewModelScope)
                observeAnnotations()
                observePageProgress()
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error(e.message ?: "Failed to open book")
            }
        }
    }

    private fun observePageProgress() {
        var saveJob: Job? = null
        viewModelScope.launch {
            currentPageFlow.collect { page ->
                saveJob?.cancel()
                saveJob = viewModelScope.launch(ioDispatcher) {
                    delay(500L)
                    val state = _uiState.value as? ReaderUiState.Success ?: return@launch
                    val totalPages = state.totalPages
                    val readPct = if (totalPages > 0) (page + 1).toDouble() / totalPages * 100.0 else 0.0
                    val now = System.currentTimeMillis()
                    val updated = state.readingProgress.copy(
                        currentPage = page,
                        readPercentage = readPct,
                        status = ReadingStatus.READING,
                        startedAt = state.readingProgress.startedAt ?: now,
                        updatedAt = now
                    )
                    upsertReadingProgress(updated)
                    _uiState.update { s ->
                        if (s is ReaderUiState.Success) s.copy(readingProgress = updated) else s
                    }
                }
            }
        }
    }

    private fun observeAnnotations() {
        viewModelScope.launch {
            getBookmarksForBook(bookId).collect { bookmarks ->
                _uiState.update { state ->
                    if (state is ReaderUiState.Success) state.copy(bookmarks = bookmarks) else state
                }
            }
        }
        viewModelScope.launch {
            getNotesForBook(bookId).collect { _notes.value = it }
        }
        viewModelScope.launch {
            getQuotesForBook(bookId).collect { _quotes.value = it }
        }
    }

    fun onPageChanged(page: Int) {
        currentPageFlow.value = page
        _uiState.update { state ->
            if (state is ReaderUiState.Success) state.copy(currentPage = page) else state
        }
    }

    fun onToggleOverlay() {
        _uiState.update { state ->
            if (state is ReaderUiState.Success) state.copy(isOverlayVisible = !state.isOverlayVisible) else state
        }
    }

    fun onToggleNightMode() {
        _uiState.update { state ->
            if (state is ReaderUiState.Success) {
                val updated = state.readingProgress.copy(isNightMode = !state.readingProgress.isNightMode)
                viewModelScope.launch(ioDispatcher) { upsertReadingProgress(updated) }
                state.copy(readingProgress = updated)
            } else state
        }
    }

    fun loadPageText() {
        val page = (_uiState.value as? ReaderUiState.Success)?.currentPage ?: return
        _pageText.value = null
        viewModelScope.launch(ioDispatcher) {
            _pageText.value = textExtractionSource.extractPageText(bookId, page)
        }
    }

    fun onAddBookmark() {
        val currentPage = (_uiState.value as? ReaderUiState.Success)?.currentPage ?: return
        viewModelScope.launch(ioDispatcher) {
            addBookmarkUseCase(Bookmark(bookId = bookId, pageNumber = currentPage))
        }
    }

    fun onDeleteBookmark(id: Long) {
        viewModelScope.launch(ioDispatcher) { deleteBookmarkUseCase(id) }
    }

    fun onAddNote(text: String, pageNumber: Int?) {
        viewModelScope.launch(ioDispatcher) {
            val now = System.currentTimeMillis()
            addNoteUseCase(Note(bookId = bookId, pageNumber = pageNumber, text = text, createdAt = now, updatedAt = now))
        }
    }

    fun onUpdateNote(note: Note) {
        viewModelScope.launch(ioDispatcher) {
            updateNoteUseCase(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun onDeleteNote(id: Long) {
        viewModelScope.launch(ioDispatcher) { deleteNoteUseCase(id) }
    }

    fun onAddQuote(text: String) {
        val currentPage = (_uiState.value as? ReaderUiState.Success)?.currentPage ?: return
        viewModelScope.launch(ioDispatcher) {
            val now = System.currentTimeMillis()
            addQuoteUseCase(Quote(bookId = bookId, pageNumber = currentPage, text = text, createdAt = now, updatedAt = now))
        }
    }

    fun onDeleteQuote(id: Long) {
        viewModelScope.launch(ioDispatcher) { deleteQuoteUseCase(id) }
    }

    override fun onCleared() {
        super.onCleared()
        worker.cancel()
        session.close()
        cache.clear()
    }
}
