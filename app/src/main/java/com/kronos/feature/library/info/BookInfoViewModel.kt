package com.kronos.feature.library.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.ReadingProgress
import com.kronos.domain.model.ReadingStatus
import com.kronos.domain.usecase.book.GetBookByIdUseCase
import com.kronos.domain.usecase.book.MoveToTrashUseCase
import com.kronos.domain.usecase.collection.AddBookToCollectionUseCase
import com.kronos.domain.usecase.collection.CreateCollectionUseCase
import com.kronos.domain.usecase.collection.GetCollectionIdsForBookUseCase
import com.kronos.domain.usecase.collection.GetCollectionsUseCase
import com.kronos.domain.usecase.collection.RemoveBookFromCollectionUseCase
import com.kronos.domain.usecase.readingprogress.ObserveReadingProgressUseCase
import com.kronos.domain.usecase.readingprogress.UpsertReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BookInfoEvent {
    object NavigateBack : BookInfoEvent()
}

@HiltViewModel
class BookInfoViewModel @Inject constructor(
    private val getBookById: GetBookByIdUseCase,
    private val observeReadingProgress: ObserveReadingProgressUseCase,
    private val upsertReadingProgress: UpsertReadingProgressUseCase,
    private val moveToTrash: MoveToTrashUseCase,
    private val getCollections: GetCollectionsUseCase,
    private val getCollectionIdsForBook: GetCollectionIdsForBookUseCase,
    private val addBookToCollection: AddBookToCollectionUseCase,
    private val removeBookFromCollection: RemoveBookFromCollectionUseCase,
    private val createCollection: CreateCollectionUseCase,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _events = Channel<BookInfoEvent>(Channel.BUFFERED)
    val events: Flow<BookInfoEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<BookInfoUiState> = combine(
        flow { emit(getBookById(bookId)) },
        observeReadingProgress(bookId),
        getCollections(),
        getCollectionIdsForBook(bookId)
    ) { book, progress, collections, collectionIds ->
        if (book == null) BookInfoUiState.Error("Book not found")
        else BookInfoUiState.Success(book, progress, collections, collectionIds.toSet())
    }
        .catch { e -> emit(BookInfoUiState.Error(e.message ?: "Unexpected error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookInfoUiState.Loading)

    fun onStatusChange(newStatus: ReadingStatus) {
        val progress = (uiState.value as? BookInfoUiState.Success)?.progress
        val now = System.currentTimeMillis()
        viewModelScope.launch(ioDispatcher) {
            val updated = progress?.copy(
                status = newStatus,
                startedAt = if (newStatus == ReadingStatus.READING && progress.startedAt == null) now else progress.startedAt,
                completedAt = if (newStatus == ReadingStatus.HAVE_READ) now else progress.completedAt
            ) ?: ReadingProgress(
                bookId = bookId,
                status = newStatus,
                updatedAt = now,
                startedAt = if (newStatus == ReadingStatus.READING) now else null,
                completedAt = if (newStatus == ReadingStatus.HAVE_READ) now else null
            )
            upsertReadingProgress(updated)
        }
    }

    fun onCollectionToggle(collectionId: Long, add: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            if (add) addBookToCollection(bookId, collectionId)
            else removeBookFromCollection(bookId, collectionId)
        }
    }

    fun onCreateAndAddCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch(ioDispatcher) {
            val collectionId = createCollection(name.trim())
            if (collectionId > 0) addBookToCollection(bookId, collectionId)
        }
    }

    fun onSaveReview(rating: Int?, review: String?) {
        val progress = (uiState.value as? BookInfoUiState.Success)?.progress ?: return
        val now = System.currentTimeMillis()
        viewModelScope.launch(ioDispatcher) {
            upsertReadingProgress(progress.copy(rating = rating, review = review, updatedAt = now))
        }
    }

    fun onMoveToTrash() {
        viewModelScope.launch(ioDispatcher) {
            moveToTrash(bookId)
            _events.send(BookInfoEvent.NavigateBack)
        }
    }
}
