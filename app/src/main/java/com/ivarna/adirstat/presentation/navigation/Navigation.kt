package com.ivarna.adirstat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ivarna.adirstat.presentation.dashboard.DashboardScreen

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Treemap : Screen("treemap/{volumePath}") {
        fun createRoute(volumePath: String) = "treemap/${volumePath.encodeForRoute()}"
    }
    data object FileList : Screen("filelist/{volumePath}") {
        fun createRoute(volumePath: String) = "filelist/${volumePath.encodeForRoute()}"
    }
    data object Duplicates : Screen("duplicates")
    data object AppStats : Screen("app_stats")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object Search : Screen("search")
}

private fun String.encodeForRoute(): String = java.net.URLEncoder.encode(this, "UTF-8")
private fun String.decodeFromRoute(): String = java.net.URLDecoder.decode(this, "UTF-8")

@Composable
fun AdirstatNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTreemap = { path ->
                    navController.navigate(Screen.Treemap.createRoute(path))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(
            route = Screen.Treemap.route,
            arguments = listOf(
                navArgument("volumePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val volumePath = backStackEntry.arguments?.getString("volumePath")?.decodeFromRoute() ?: ""
            com.ivarna.adirstat.presentation.treemap.TreemapScreen(
                volumePath = volumePath,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFileList = {
                    navController.navigate(Screen.FileList.createRoute(volumePath))
                }
            )
        }
        
        composable(
            route = Screen.FileList.route,
            arguments = listOf(
                navArgument("volumePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val volumePath = backStackEntry.arguments?.getString("volumePath")?.decodeFromRoute() ?: ""
            com.ivarna.adirstat.presentation.filelist.FileListScreen(
                volumePath = volumePath,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Duplicates.route) {
            com.ivarna.adirstat.presentation.duplicates.DuplicatesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AppStats.route) {
            com.ivarna.adirstat.presentation.appstats.AppStatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            com.ivarna.adirstat.presentation.settings.SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.History.route) {
            com.ivarna.adirstat.presentation.history.HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Search.route) {
            com.ivarna.adirstat.presentation.search.SearchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
