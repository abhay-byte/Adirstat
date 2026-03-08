package com.ivarna.adirstat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ivarna.adirstat.presentation.dashboard.DashboardScreen
import com.ivarna.adirstat.presentation.permission.PermissionScreen

sealed class Screen(val route: String) {
    data object Permission : Screen("permission")
    data object Dashboard : Screen("dashboard")
    data object Treemap : Screen("treemap/{volumePath}") {
        fun createRoute(volumePath: String) = "treemap/${volumePath.encodeForRoute()}"
    }
    data object FileList : Screen("filelist/{volumePath}?rootPath={rootPath}") {
        fun createRoute(volumePath: String, rootPath: String? = null): String {
            val encodedVolumePath = volumePath.encodeForRoute()
            val encodedRootPath = rootPath?.takeIf { it.isNotBlank() }?.encodeForRoute()
            return if (encodedRootPath != null) {
                "filelist/$encodedVolumePath?rootPath=$encodedRootPath"
            } else {
                "filelist/$encodedVolumePath"
            }
        }
    }
    data object Duplicates : Screen("duplicates")
    data object AppStats : Screen("app_stats")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object Search : Screen("search?rootPath={rootPath}&scopePath={scopePath}") {
        fun createRoute(rootPath: String? = null, scopePath: String? = null): String {
            val queryParams = buildList {
                rootPath?.takeIf { it.isNotBlank() }?.let {
                    add("rootPath=${it.encodeForRoute()}")
                }
                scopePath?.takeIf { it.isNotBlank() }?.let {
                    add("scopePath=${it.encodeForRoute()}")
                }
            }
            return if (queryParams.isEmpty()) {
                "search"
            } else {
                "search?${queryParams.joinToString("&")}" 
            }
        }
    }
}

private fun String.encodeForRoute(): String = java.net.URLEncoder.encode(this, "UTF-8")
private fun String.decodeFromRoute(): String = java.net.URLDecoder.decode(this, "UTF-8")

@Composable
fun AdirstatNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Permission.route
    ) {
        composable(Screen.Permission.route) {
            PermissionScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }
        
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
                onNavigateToSearch = { rootPath, scopePath ->
                    navController.navigate(Screen.Search.createRoute(rootPath, scopePath))
                },
                onNavigateToFileList = { path ->
                    navController.navigate(Screen.FileList.createRoute(path, volumePath))
                }
            )
        }
        
        composable(
            route = Screen.FileList.route,
            arguments = listOf(
                navArgument("volumePath") { type = NavType.StringType },
                navArgument("rootPath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val volumePath = backStackEntry.arguments?.getString("volumePath")?.decodeFromRoute() ?: ""
            val rootPath = backStackEntry.arguments?.getString("rootPath")?.decodeFromRoute()
            com.ivarna.adirstat.presentation.filelist.FileListScreen(
                volumePath = volumePath,
                rootPath = rootPath,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSearch = { searchRootPath, scopePath ->
                    navController.navigate(Screen.Search.createRoute(searchRootPath, scopePath))
                }
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
        
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("rootPath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("scopePath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val rootPath = backStackEntry.arguments?.getString("rootPath")?.decodeFromRoute()
            val scopePath = backStackEntry.arguments?.getString("scopePath")?.decodeFromRoute()
            com.ivarna.adirstat.presentation.search.SearchScreen(
                rootPath = rootPath,
                scopePath = scopePath,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDirectory = { path, searchRootPath ->
                    navController.navigate(Screen.FileList.createRoute(path, searchRootPath))
                }
            )
        }
    }
}
