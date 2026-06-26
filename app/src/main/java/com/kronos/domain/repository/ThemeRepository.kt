package com.kronos.domain.repository

import com.kronos.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
