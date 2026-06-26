package com.kronos.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kronos.domain.model.ThemeMode
import com.kronos.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    override val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val stored = prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
            runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE_KEY] = mode.name }
    }
}
