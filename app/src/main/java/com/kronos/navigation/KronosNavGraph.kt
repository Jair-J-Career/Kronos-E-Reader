package com.kronos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kronos.feature.library.LibraryScreen
import com.kronos.feature.reader.ReaderScreen
import com.kronos.feature.settings.SettingsScreen

@Composable
fun KronosNavGraph(navController: NavHostController = rememberNavController()) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val topLevelRoutes = setOf(NavRoutes.LIBRARY, NavRoutes.SETTINGS)

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                KronosBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.LIBRARY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.LIBRARY) {
                LibraryScreen(onBookClick = { navController.navigate(NavRoutes.reader(it)) })
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = NavRoutes.READER,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) {
                ReaderScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun KronosBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == NavRoutes.LIBRARY,
            onClick = {
                navController.navigate(NavRoutes.LIBRARY) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Library") }
        )
        NavigationBarItem(
            selected = currentRoute == NavRoutes.SETTINGS,
            onClick = {
                navController.navigate(NavRoutes.SETTINGS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") }
        )
    }
}
