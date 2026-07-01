package com.kronos.feature.library.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.domain.model.Collection
import com.kronos.domain.usecase.collection.CreateCollectionUseCase
import com.kronos.domain.usecase.collection.GetCollectionBookCountsUseCase
import com.kronos.domain.usecase.collection.GetCollectionCoversUseCase
import com.kronos.domain.usecase.collection.GetCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CollectionViewMode { QUICK, COVERS, GRID }

sealed class CollectionsUiState {
    object Loading : CollectionsUiState()
    data class Success(val collections: List<Collection>) : CollectionsUiState()
    data class Error(val message: String) : CollectionsUiState()
}

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    getCollections: GetCollectionsUseCase,
    getCollectionBookCounts: GetCollectionBookCountsUseCase,
    getCollectionCovers: GetCollectionCoversUseCase,
    private val createCollection: CreateCollectionUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: StateFlow<CollectionsUiState> = getCollections()
        .map<List<Collection>, CollectionsUiState> { CollectionsUiState.Success(it) }
        .catch { e -> emit(CollectionsUiState.Error(e.message ?: "Unexpected error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsUiState.Loading)

    val bookCounts: StateFlow<Map<Long, Int>> = getCollectionBookCounts()
        .catch { emit(emptyMap()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val collectionCovers: StateFlow<Map<Long, List<String>>> = getCollectionCovers()
        .catch { emit(emptyMap()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _viewMode = MutableStateFlow(
        savedStateHandle.get<String>(KEY_VIEW_MODE)
            ?.let { runCatching { CollectionViewMode.valueOf(it) }.getOrNull() }
            ?: CollectionViewMode.COVERS
    )
    val viewMode: StateFlow<CollectionViewMode> = _viewMode.asStateFlow()

    fun onViewModeChange(mode: CollectionViewMode) {
        savedStateHandle[KEY_VIEW_MODE] = mode.name
        _viewMode.value = mode
    }

    fun onCreateCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { createCollection(name.trim()) }
    }

    companion object {
        private const val KEY_VIEW_MODE = "collections_view_mode"
    }
}
