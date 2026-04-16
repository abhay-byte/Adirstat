package com.ivarna.adirstat.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

private val bottomNavScreens = listOf(Screen.Dashboard, Screen.AppStats, Screen.History, Screen.Settings)

@Composable
fun AdirstatNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Permission.route,
            modifier = Modifier.padding(padding)
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
}

@Composable
private fun AppBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = Color.Transparent,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.Apps,
                    label = "Dashboard",
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = { onNavigate(Screen.Dashboard.route) }
                )
                BottomNavItem(
                    icon = Icons.Default.Apps,
                    label = "Apps",
                    selected = currentRoute == Screen.AppStats.route,
                    onClick = { onNavigate(Screen.AppStats.route) }
                )
                BottomNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    selected = currentRoute == Screen.History.route,
                    onClick = { onNavigate(Screen.History.route) }
                )
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { onNavigate(Screen.Settings.route) }
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
    
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
