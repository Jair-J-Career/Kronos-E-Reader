package com.kronos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.domain.model.ThemeMode
import com.kronos.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = themeRepository.themeMode
        .map<ThemeMode, SettingsUiState> { SettingsUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading
        )

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { themeRepository.setThemeMode(mode) }
    }
}
