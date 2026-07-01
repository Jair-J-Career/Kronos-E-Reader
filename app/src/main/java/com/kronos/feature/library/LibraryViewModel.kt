package com.kronos.feature.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.Book
import com.kronos.domain.model.ReadingStatus
import com.kronos.domain.model.SortMode
import com.kronos.domain.model.ViewMode
import com.kronos.domain.usecase.book.AddSpecificFilesUseCase
import com.kronos.domain.usecase.book.GetAllBooksUseCase
import com.kronos.domain.usecase.book.GetBooksByStatusUseCase
import com.kronos.domain.usecase.book.ScanFolderUseCase
import com.kronos.domain.usecase.readingprogress.ObserveAllReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val getBooksByStatusUseCase: GetBooksByStatusUseCase,
    private val scanFolderUseCase: ScanFolderUseCase,
    private val addSpecificFilesUseCase: AddSpecificFilesUseCase,
    private val observeAllProgress: ObserveAllReadingProgressUseCase,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
    val events: Flow<LibraryEvent> = _events.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _viewModes = MutableStateFlow(restoreViewModes())
    private val _sortModes = MutableStateFlow(restoreSortModes())
    private val _statusFilter = MutableStateFlow<ReadingStatus?>(null)

    private val booksFlow: StateFlow<List<Book>> = combine(_sortModes, _statusFilter) { sortModes, status ->
        (sortModes[status] ?: SortMode.RECENT) to status
    }.flatMapLatest { (sort, status) ->
        if (status == null) getAllBooksUseCase(sort)
        else getBooksByStatusUseCase(status, sort)
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<LibraryUiState> = combine(
        booksFlow,
        _searchQuery,
        _viewModes,
        _sortModes,
        _statusFilter
    ) { books, query, viewModes, sortModes, status ->
        val viewMode = viewModes[status] ?: ViewMode.COMPLETE
        val sortMode = sortModes[status] ?: SortMode.RECENT
        val filtered = if (query.isBlank()) books else books.filter {
            it.title.contains(query, ignoreCase = true)
        }
        LibraryUiState.Success(filtered, viewMode, sortMode) as LibraryUiState
    }
        .catch { e -> emit(LibraryUiState.Error(e.message ?: "Unexpected error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState.Loading
        )

    val progressMap: StateFlow<Map<Long, Float>> = observeAllProgress()
        .map { list ->
            list.associate { p ->
                p.bookId to (p.readPercentage / 100.0).toFloat().coerceIn(0f, 1f)
            }
        }
        .catch { emit(emptyMap()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun onViewModeChange(mode: ViewMode) {
        val status = _statusFilter.value
        savedStateHandle["view_${status?.name ?: "all"}"] = mode.name
        _viewModes.update { it + (status to mode) }
    }

    fun onSortModeChange(mode: SortMode) {
        val status = _statusFilter.value
        savedStateHandle["sort_${status?.name ?: "all"}"] = mode.name
        _sortModes.update { it + (status to mode) }
    }

    fun onStatusFilterChange(status: ReadingStatus?) { _statusFilter.value = status }

    fun onGrantFolderAccess() {
        viewModelScope.launch { _events.send(LibraryEvent.OpenFolderPicker) }
    }

    fun onGrantFileAccess() {
        viewModelScope.launch { _events.send(LibraryEvent.OpenFilePicker) }
    }

    fun onTreeUriGranted(uri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            appContext.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            scanFolderUseCase(uri)
        }
    }

    fun onFilesGranted(uris: List<Uri>) {
        viewModelScope.launch(ioDispatcher) {
            uris.forEach { uri ->
                appContext.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            addSpecificFilesUseCase(uris)
        }
    }

    private fun restoreViewModes(): Map<ReadingStatus?, ViewMode> {
        val map = mutableMapOf<ReadingStatus?, ViewMode>()
        savedStateHandle.get<String>("view_all")
            ?.let { runCatching { ViewMode.valueOf(it) }.getOrNull() }
            ?.let { map[null] = it }
        ReadingStatus.entries.forEach { status ->
            savedStateHandle.get<String>("view_${status.name}")
                ?.let { runCatching { ViewMode.valueOf(it) }.getOrNull() }
                ?.let { map[status] = it }
        }
        return map
    }

    private fun restoreSortModes(): Map<ReadingStatus?, SortMode> {
        val map = mutableMapOf<ReadingStatus?, SortMode>()
        savedStateHandle.get<String>("sort_all")
            ?.let { runCatching { SortMode.valueOf(it) }.getOrNull() }
            ?.let { map[null] = it }
        ReadingStatus.entries.forEach { status ->
            savedStateHandle.get<String>("sort_${status.name}")
                ?.let { runCatching { SortMode.valueOf(it) }.getOrNull() }
                ?.let { map[status] = it }
        }
        return map
    }
}

sealed class LibraryEvent {
    object OpenFolderPicker : LibraryEvent()
    object OpenFilePicker : LibraryEvent()
}
