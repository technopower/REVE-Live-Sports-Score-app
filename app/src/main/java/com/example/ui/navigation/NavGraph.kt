package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.leagues.LeaguesScreen
import com.example.ui.screens.live.LiveMatchesScreen
import com.example.ui.screens.matchdetails.MatchDetailsScreen
import com.example.ui.screens.news.NewsScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.SportsViewModel

sealed class Screen(
    val route: String,
    val title: String? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Splash : Screen("splash")
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Live : Screen("live", "Live", Icons.Filled.Bolt, Icons.Outlined.Bolt)
    object Leagues : Screen("leagues", "Leagues", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object News : Screen("news", "News", Icons.Filled.Article, Icons.Outlined.Article)
    object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)

    object MatchDetails : Screen("match_details/{matchId}") {
        fun createRoute(matchId: String) = "match_details/$matchId"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Live,
    Screen.Leagues,
    Screen.News,
    Screen.Favorites
)

@Composable
fun ReveNavGraph(
    viewModel: SportsViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SportsCard,
                    contentColor = SportsNeonGreen,
                    tonalElevation = 8.dp
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon!! else screen.unselectedIcon!!,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) {
                                        if (screen == Screen.Live) SportsLiveRed else SportsNeonGreen
                                    } else SportsTextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SportsTextPrimary else SportsTextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SportsCardSecondary
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        containerColor = SportsBackground,
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onMatchClick = { matchId ->
                        navController.navigate(Screen.MatchDetails.createRoute(matchId))
                    },
                    onLeagueClick = { leagueId ->
                        navController.navigate(Screen.Leagues.route)
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Live.route) {
                LiveMatchesScreen(
                    viewModel = viewModel,
                    onMatchClick = { matchId ->
                        navController.navigate(Screen.MatchDetails.createRoute(matchId))
                    }
                )
            }

            composable(Screen.Leagues.route) {
                LeaguesScreen(
                    viewModel = viewModel,
                    onLeagueClick = {
                        // Could open league details or filter
                    }
                )
            }

            composable(Screen.News.route) {
                NewsScreen(viewModel = viewModel)
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onMatchClick = { matchId ->
                        navController.navigate(Screen.MatchDetails.createRoute(matchId))
                    }
                )
            }

            composable(
                route = Screen.MatchDetails.route,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                MatchDetailsScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onMatchClick = { matchId ->
                        navController.navigate(Screen.MatchDetails.createRoute(matchId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
