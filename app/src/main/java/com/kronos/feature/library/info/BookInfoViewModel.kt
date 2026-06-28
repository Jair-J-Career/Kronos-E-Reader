package com.kronos.feature.library.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.usecase.book.GetBookByIdUseCase
import com.kronos.domain.usecase.readingprogress.GetReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookInfoViewModel @Inject constructor(
    private val getBookById: GetBookByIdUseCase,
    private val getReadingProgress: GetReadingProgressUseCase,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow<BookInfoUiState>(BookInfoUiState.Loading)
    val uiState: StateFlow<BookInfoUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(ioDispatcher) {
            val book = getBookById(bookId)
            if (book == null) {
                _uiState.value = BookInfoUiState.Error("Book not found")
                return@launch
            }
            _uiState.value = BookInfoUiState.Success(
                book = book,
                progress = getReadingProgress(bookId)
            )
        }
    }
}
