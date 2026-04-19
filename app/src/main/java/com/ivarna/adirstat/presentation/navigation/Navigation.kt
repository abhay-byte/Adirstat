package com.ivarna.adirstat.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ivarna.adirstat.presentation.appstats.AppStatsScreen
import com.ivarna.adirstat.presentation.dashboard.DashboardScreen
import com.ivarna.adirstat.presentation.duplicates.DuplicatesScreen
import com.ivarna.adirstat.presentation.filelist.FileListScreen
import com.ivarna.adirstat.presentation.history.HistoryScreen
import com.ivarna.adirstat.presentation.permission.PermissionScreen
import com.ivarna.adirstat.presentation.search.SearchScreen
import com.ivarna.adirstat.presentation.settings.SettingsScreen
import com.ivarna.adirstat.presentation.treemap.TreemapScreen

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

private val bottomNavScreens = listOf(Screen.Dashboard, Screen.AppStats, Screen.History, Screen.Settings)

@Composable
fun AdirstatNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Delegate insets to inner screens
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Permission.route,
            modifier = Modifier.fillMaxSize()
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
                TreemapScreen(
                    volumePath = volumePath,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSearch = { vol, scope ->
                        navController.navigate(Screen.Search.createRoute(vol, scope))
                    }
                )
            }

            composable(
                route = Screen.FileList.route,
                arguments = listOf(
                    navArgument("volumePath") { type = NavType.StringType },
                    navArgument("rootPath") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val volumePath = backStackEntry.arguments?.getString("volumePath")?.decodeFromRoute() ?: ""
                val rootPath = backStackEntry.arguments?.getString("rootPath")?.decodeFromRoute()
                FileListScreen(
                    volumePath = volumePath,
                    rootPath = rootPath,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSearch = { vol, scope ->
                        navController.navigate(Screen.Search.createRoute(vol, scope))
                    }
                )
            }

            composable(
                route = Screen.Search.route,
                arguments = listOf(
                    navArgument("rootPath") { type = NavType.StringType },
                    navArgument("scopePath") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val rootPath = backStackEntry.arguments?.getString("rootPath")?.decodeFromRoute() ?: ""
                val scopePath = backStackEntry.arguments?.getString("scopePath")?.decodeFromRoute()
                SearchScreen(
                    rootPath = rootPath,
                    scopePath = scopePath,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDirectory = { path, root ->
                        navController.navigate(Screen.FileList.createRoute(root ?: rootPath, path))
                    }
                )
            }

            composable(Screen.AppStats.route) {
                AppStatsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Duplicates.route) {
                DuplicatesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val screens = listOf(
                Triple(Screen.Dashboard, Icons.Default.Home, "Dashboard"),
                Triple(Screen.AppStats, Icons.Default.Apps, "Apps"),
                Triple(Screen.History, Icons.Default.History, "History"),
                Triple(Screen.Settings, Icons.Default.Settings, "Settings")
            )

            screens.forEach { (screen, icon, label) ->
                BottomNavItem(
                    icon = icon,
                    label = label,
                    selected = currentRoute == screen.route,
                    onClick = { onNavigate(screen.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
