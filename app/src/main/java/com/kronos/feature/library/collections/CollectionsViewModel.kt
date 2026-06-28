package com.kronos.feature.library.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.domain.model.Collection
import com.kronos.domain.usecase.collection.CreateCollectionUseCase
import com.kronos.domain.usecase.collection.GetCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CollectionsUiState {
    object Loading : CollectionsUiState()
    data class Success(val collections: List<Collection>) : CollectionsUiState()
    data class Error(val message: String) : CollectionsUiState()
}

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    getCollections: GetCollectionsUseCase,
    private val createCollection: CreateCollectionUseCase
) : ViewModel() {

    val uiState: StateFlow<CollectionsUiState> = getCollections()
        .map<List<Collection>, CollectionsUiState> { CollectionsUiState.Success(it) }
        .catch { e -> emit(CollectionsUiState.Error(e.message ?: "Unexpected error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsUiState.Loading)

    fun onCreateCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { createCollection(name.trim()) }
    }
}
