package com.kronos.feature.library.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.domain.model.BookAnnotationSummary
import com.kronos.domain.usecase.book.GetBooksWithAnnotationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class AnnotationSort { RECENT, MOST }

@HiltViewModel
class AnnotationsViewModel @Inject constructor(
    getBooksWithAnnotations: GetBooksWithAnnotationsUseCase
) : ViewModel() {

    private val _sort = MutableStateFlow(AnnotationSort.RECENT)
    val sort: StateFlow<AnnotationSort> = _sort.asStateFlow()

    val items: StateFlow<List<BookAnnotationSummary>> = combine(
        getBooksWithAnnotations(),
        _sort
    ) { books, sort ->
        when (sort) {
            AnnotationSort.RECENT -> books.sortedByDescending { it.latestAnnotationAt }
            AnnotationSort.MOST -> books.sortedByDescending { it.quoteCount + it.noteCount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleSort() {
        _sort.value = if (_sort.value == AnnotationSort.RECENT) AnnotationSort.MOST else AnnotationSort.RECENT
    }
}
