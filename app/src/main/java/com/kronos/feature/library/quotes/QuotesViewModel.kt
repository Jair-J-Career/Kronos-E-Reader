package com.kronos.feature.library.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.domain.model.QuoteSummary
import com.kronos.domain.usecase.quote.GetAllQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(
    getAllQuotes: GetAllQuotesUseCase
) : ViewModel() {

    val quotes: StateFlow<List<QuoteSummary>> = getAllQuotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
