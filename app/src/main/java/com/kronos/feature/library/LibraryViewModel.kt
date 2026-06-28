package com.kronos.feature.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.Book
import com.kronos.domain.usecase.book.AddSpecificFilesUseCase
import com.kronos.domain.usecase.book.GetAllBooksUseCase
import com.kronos.domain.usecase.book.ScanFolderUseCase
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    getAllBooksUseCase: GetAllBooksUseCase,
    private val scanFolderUseCase: ScanFolderUseCase,
    private val addSpecificFilesUseCase: AddSpecificFilesUseCase,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
    val events: Flow<LibraryEvent> = _events.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        getAllBooksUseCase(),
        _searchQuery
    ) { books: List<Book>, query: String ->
        val filtered = if (query.isBlank()) books else books.filter {
            it.title.contains(query, ignoreCase = true)
        }
        LibraryUiState.Success(filtered) as LibraryUiState
    }
        .catch { e -> emit(LibraryUiState.Error(e.message ?: "Unexpected error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState.Loading
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

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
}

sealed class LibraryEvent {
    object OpenFolderPicker : LibraryEvent()
    object OpenFilePicker : LibraryEvent()
}
