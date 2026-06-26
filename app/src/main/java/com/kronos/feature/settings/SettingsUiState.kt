package com.kronos.feature.settings

import com.kronos.domain.model.ThemeMode

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val themeMode: ThemeMode) : SettingsUiState()
}
