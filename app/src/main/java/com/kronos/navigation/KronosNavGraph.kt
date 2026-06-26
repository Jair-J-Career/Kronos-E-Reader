package com.kronos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kronos.feature.library.LibraryScreen
import com.kronos.feature.reader.ReaderScreen

@Composable
fun KronosNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NavRoutes.LIBRARY) {
        composable(NavRoutes.LIBRARY) {
            LibraryScreen(
                onBookClick = { bookId -> navController.navigate(NavRoutes.reader(bookId)) }
            )
        }
        composable(
            route = NavRoutes.READER,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) {
            ReaderScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
