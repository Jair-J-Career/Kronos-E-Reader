package com.kronos.feature.library.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.Book
import com.kronos.domain.usecase.book.EmptyTrashUseCase
import com.kronos.domain.usecase.book.GetTrashedBooksUseCase
import com.kronos.domain.usecase.book.RestoreFromTrashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    getTrashedBooks: GetTrashedBooksUseCase,
    private val restoreFromTrash: RestoreFromTrashUseCase,
    private val emptyTrash: EmptyTrashUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val books: StateFlow<List<Book>> = getTrashedBooks()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onRestoreBook(bookId: Long) {
        viewModelScope.launch(ioDispatcher) { restoreFromTrash(bookId) }
    }

    fun onEmptyTrash() {
        viewModelScope.launch(ioDispatcher) { emptyTrash() }
    }
}
