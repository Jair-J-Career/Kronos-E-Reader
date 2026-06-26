package com.kronos.feature.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.Book
import com.kronos.domain.usecase.book.GetAllBooksUseCase
import com.kronos.domain.usecase.book.ScanFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    getAllBooksUseCase: GetAllBooksUseCase,
    private val scanFolderUseCase: ScanFolderUseCase,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
    val events: Flow<LibraryEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<LibraryUiState> = getAllBooksUseCase()
        .map<List<Book>, LibraryUiState> { LibraryUiState.Success(it) }
        .catch { e -> emit(LibraryUiState.Error(e.message ?: "Unexpected error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState.Loading
        )

    fun onGrantFolderAccess() {
        viewModelScope.launch { _events.send(LibraryEvent.OpenSafPicker) }
    }

    fun onTreeUriGranted(uri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            appContext.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            scanFolderUseCase(uri)
        }
    }
}

sealed class LibraryEvent {
    object OpenSafPicker : LibraryEvent()
}
