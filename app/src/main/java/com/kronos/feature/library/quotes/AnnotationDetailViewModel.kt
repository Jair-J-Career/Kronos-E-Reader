package com.kronos.feature.library.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.domain.model.Note
import com.kronos.domain.model.Quote
import com.kronos.domain.usecase.note.GetNotesForBookUseCase
import com.kronos.domain.usecase.quote.GetQuotesForBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class AnnotationItems {
    object Loading : AnnotationItems()
    data class Quotes(val items: List<Quote>) : AnnotationItems()
    data class Notes(val items: List<Note>) : AnnotationItems()
}

@HiltViewModel
class AnnotationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getQuotesForBook: GetQuotesForBookUseCase,
    getNotesForBook: GetNotesForBookUseCase
) : ViewModel() {

    val bookId: Long = checkNotNull(savedStateHandle["bookId"])
    val type: String = checkNotNull(savedStateHandle["type"])

    val items: StateFlow<AnnotationItems> = if (type == "quotes") {
        getQuotesForBook(bookId)
            .map { list -> AnnotationItems.Quotes(list.sortedBy { it.pageNumber }) }
            .catch { emit(AnnotationItems.Loading) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnnotationItems.Loading)
    } else {
        getNotesForBook(bookId)
            .map { list -> AnnotationItems.Notes(list.sortedBy { it.pageNumber }) }
            .catch { emit(AnnotationItems.Loading) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnnotationItems.Loading)
    }
}
