package com.kronos.feature.library.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.Book
import com.kronos.domain.usecase.collection.GetBooksInCollectionUseCase
import com.kronos.domain.usecase.collection.GetCollectionByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CollectionDetailUiState {
    object Loading : CollectionDetailUiState()
    data class Success(val collectionName: String, val books: List<Book>) : CollectionDetailUiState()
    data class Error(val message: String) : CollectionDetailUiState()
}

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val getBooksInCollection: GetBooksInCollectionUseCase,
    private val getCollectionById: GetCollectionByIdUseCase,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val collectionId: Long = checkNotNull(savedStateHandle["collectionId"])
    private val _collectionName = MutableStateFlow("")

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        _collectionName,
        getBooksInCollection(collectionId)
    ) { name, books ->
        CollectionDetailUiState.Success(name, books) as CollectionDetailUiState
    }
        .catch { e -> emit(CollectionDetailUiState.Error(e.message ?: "Unexpected error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionDetailUiState.Loading)

    init {
        viewModelScope.launch(ioDispatcher) {
            _collectionName.value = getCollectionById(collectionId)?.name ?: ""
        }
    }
}
