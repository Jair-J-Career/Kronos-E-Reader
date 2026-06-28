package com.kronos.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kronos.feature.library.LibraryScreen
import com.kronos.feature.library.info.BookInfoScreen
import com.kronos.feature.reader.ReaderScreen
import com.kronos.feature.reader.progress.ProgressDetailScreen
import com.kronos.feature.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun KronosNavGraph(navController: NavHostController = rememberNavController()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == NavRoutes.LIBRARY,
        drawerContent = {
            KronosDrawerSheet(
                currentRoute = currentRoute,
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    navController.navigate(NavRoutes.SETTINGS) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.LIBRARY,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable(NavRoutes.LIBRARY) {
                LibraryScreen(
                    onBookCoverClick = { navController.navigate(NavRoutes.reader(it)) },
                    onBookTitleClick = { navController.navigate(NavRoutes.info(it)) },
                    onBookProgressClick = { navController.navigate(NavRoutes.progress(it)) },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
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
            composable(
                route = NavRoutes.INFO,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) {
                BookInfoScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = NavRoutes.PROGRESS,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) {
                ProgressDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun KronosDrawerSheet(
    currentRoute: String?,
    onNavigateToSettings: () -> Unit,
    onItemClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Kronos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("In Progress") },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
                    selected = false,
                    onClick = onItemClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Books & Docs") },
                    icon = { Icon(Icons.Default.LibraryBooks, contentDescription = null) },
                    selected = currentRoute == NavRoutes.LIBRARY,
                    onClick = onItemClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("To Do") },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    selected = false,
                    onClick = onItemClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Collections") },
                    icon = { Icon(Icons.Default.Collections, contentDescription = null) },
                    selected = false,
                    onClick = onItemClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Quotes") },
                    icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                    selected = false,
                    onClick = onItemClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Downloads") },
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    selected = false,
                    onClick = onItemClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Trash") },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    selected = false,
                    onClick = onItemClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            HorizontalDivider()
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = currentRoute == NavRoutes.SETTINGS,
                onClick = onNavigateToSettings,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            NavigationDrawerItem(
                label = { Text("Feedback") },
                icon = { Icon(Icons.Default.Feedback, contentDescription = null) },
                selected = false,
                onClick = onItemClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
