package com.kronos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.domain.model.ThemeMode
import com.kronos.domain.repository.ThemeRepository
import com.kronos.navigation.KronosNavGraph
import com.kronos.ui.theme.KronosTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            KronosTheme(
                darkTheme = isDark,
                dynamicColor = themeMode == ThemeMode.SYSTEM
            ) {
                KronosNavGraph()
            }
        }
    }
}
