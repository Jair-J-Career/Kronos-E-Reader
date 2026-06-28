package com.kronos.navigation

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kronos.domain.model.ReadingStatus
import com.kronos.feature.library.LibraryScreen
import com.kronos.feature.library.collections.CollectionDetailScreen
import com.kronos.feature.library.collections.CollectionsScreen
import com.kronos.feature.library.info.BookInfoScreen
import com.kronos.feature.library.quotes.AnnotationDetailScreen
import com.kronos.feature.library.quotes.AnnotationsScreen
import com.kronos.feature.library.trash.TrashScreen
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
    var statusFilter by remember { mutableStateOf<ReadingStatus?>(null) }
    val filePickerRequests = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == NavRoutes.LIBRARY,
        drawerContent = {
            KronosDrawerSheet(
                currentRoute = currentRoute,
                activeStatusFilter = statusFilter,
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    navController.navigate(NavRoutes.SETTINGS) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToCollections = {
                    scope.launch { drawerState.close() }
                    navController.navigate(NavRoutes.COLLECTIONS) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQuotes = {
                    scope.launch { drawerState.close() }
                    navController.navigate(NavRoutes.QUOTES) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToTrash = {
                    scope.launch { drawerState.close() }
                    navController.navigate(NavRoutes.TRASH) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onStatusFilter = { filter ->
                    statusFilter = filter
                    scope.launch { drawerState.close() }
                    navController.navigate(NavRoutes.LIBRARY) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onDownloadsClick = {
                    scope.launch { drawerState.close() }
                    filePickerRequests.tryEmit(Unit)
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
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    statusFilter = statusFilter,
                    filePickerSignal = filePickerRequests,
                    onFilesAdded = { statusFilter = ReadingStatus.READING }
                )
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(NavRoutes.COLLECTIONS) {
                CollectionsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCollectionClick = { navController.navigate(NavRoutes.collectionDetail(it)) }
                )
            }
            composable(NavRoutes.QUOTES) {
                AnnotationsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onQuotesClick = { navController.navigate(NavRoutes.annotationDetail(it, "quotes")) },
                    onNotesClick = { navController.navigate(NavRoutes.annotationDetail(it, "notes")) }
                )
            }
            composable(
                route = NavRoutes.ANNOTATION_DETAIL,
                arguments = listOf(
                    navArgument("bookId") { type = NavType.LongType },
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                val type = backStackEntry.arguments?.getString("type").orEmpty()
                if (bookId <= 0L || type.isEmpty()) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                AnnotationDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(NavRoutes.TRASH) {
                TrashScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = NavRoutes.COLLECTION_DETAIL,
                arguments = listOf(navArgument("collectionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: 0L
                if (collectionId <= 0L) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                CollectionDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onBookClick = { navController.navigate(NavRoutes.info(it)) }
                )
            }
            composable(
                route = NavRoutes.READER,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                if (bookId <= 0L) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                ReaderScreen(onNavigateBack = {
                    statusFilter = ReadingStatus.READING
                    navController.popBackStack()
                })
            }
            composable(
                route = NavRoutes.INFO,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                if (bookId <= 0L) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                BookInfoScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCoverClick = { navController.navigate(NavRoutes.reader(it)) }
                )
            }
            composable(
                route = NavRoutes.PROGRESS,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                if (bookId <= 0L) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                ProgressDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun KronosDrawerSheet(
    currentRoute: String?,
    activeStatusFilter: ReadingStatus?,
    onNavigateToSettings: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToQuotes: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onStatusFilter: (ReadingStatus?) -> Unit,
    onDownloadsClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val context = LocalContext.current

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
                    selected = currentRoute == NavRoutes.LIBRARY && activeStatusFilter == ReadingStatus.READING,
                    onClick = { onStatusFilter(ReadingStatus.READING) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Completed") },
                    icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null) },
                    selected = currentRoute == NavRoutes.LIBRARY && activeStatusFilter == ReadingStatus.HAVE_READ,
                    onClick = { onStatusFilter(ReadingStatus.HAVE_READ) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("To Do") },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    selected = currentRoute == NavRoutes.LIBRARY && activeStatusFilter == ReadingStatus.TO_READ,
                    onClick = { onStatusFilter(ReadingStatus.TO_READ) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Collections") },
                    icon = { Icon(Icons.Default.Collections, contentDescription = null) },
                    selected = currentRoute == NavRoutes.COLLECTIONS,
                    onClick = onNavigateToCollections,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Quotes & Notes") },
                    icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                    selected = currentRoute == NavRoutes.QUOTES,
                    onClick = onNavigateToQuotes,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Downloads") },
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    selected = false,
                    onClick = onDownloadsClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Trash") },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    selected = currentRoute == NavRoutes.TRASH,
                    onClick = onNavigateToTrash,
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
                onClick = {
                    onItemClick()
                    Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
