package en.entouche.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import en.entouche.ui.components.GlassBottomNavBar
import en.entouche.ui.components.GradientBackground
import en.entouche.ui.components.NavItem
import en.entouche.ui.screens.*
import en.entouche.ui.theme.Dimensions
import en.entouche.ui.theme.TealWave
import en.entouche.ui.viewmodel.AuthViewModel
import en.entouche.ui.viewmodel.EntoucheViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Notes : Screen("notes")
    object NewNote : Screen("new_note")
    object NoteDetail : Screen("note/{noteId}") {
        fun createRoute(noteId: String) = "note/$noteId"
    }
    object Voice : Screen("voice")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object MemoryGame : Screen("memory_game")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Create ViewModels at the navigation level so they're shared across screens
    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
    val entoucheViewModel: EntoucheViewModel = viewModel { EntoucheViewModel() }

    val authState by authViewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Auth.route

    // Show loading while checking auth state
    if (authState.isLoading) {
        GradientBackground(animated = true) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealWave)
            }
        }
        return
    }

    // Handle auth state changes - navigate accordingly
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            // User is authenticated, go to home
            if (currentRoute == Screen.Auth.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
            // Refresh data when authenticated
            entoucheViewModel.loadNotes()
            entoucheViewModel.loadStats()
        } else {
            // User is not authenticated, go to auth
            if (currentRoute != Screen.Auth.route) {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Determine if bottom nav should be shown
    val showBottomNav = remember(currentRoute, authState.isAuthenticated) {
        authState.isAuthenticated && currentRoute in listOf(
            Screen.Home.route,
            Screen.Notes.route,
            Screen.Voice.route,
            Screen.Search.route,
            Screen.Settings.route
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (authState.isAuthenticated) Screen.Home.route else Screen.Auth.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // Auth Screen
            composable(
                route = Screen.Auth.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                AuthScreen(viewModel = authViewModel)
            }

            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                HomeScreen(
                    viewModel = entoucheViewModel,
                    onNavigateToNotes = {
                        navController.navigate(Screen.Notes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToVoice = {
                        navController.navigate(Screen.Voice.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToNoteDetail = { noteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                    },
                    onNavigateToNewNote = {
                        navController.navigate(Screen.NewNote.route)
                    },
                    onNavigateToMemoryGame = {
                        navController.navigate(Screen.MemoryGame.route)
                    },
                    userName = authState.userName ?: authState.userEmail?.substringBefore("@"),
                    modifier = Modifier.padding(bottom = if (showBottomNav) Dimensions.bottomNavHeight else Dimensions.spacingXs)
                )
            }

            composable(
                route = Screen.Notes.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                NotesListScreen(
                    onNavigateToNoteDetail = { noteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                    },
                    onNavigateToNewNote = {
                        navController.navigate(Screen.NewNote.route)
                    },
                    modifier = Modifier.padding(bottom = if (showBottomNav) Dimensions.bottomNavHeight else Dimensions.spacingXs)
                )
            }

            // New Note Editor Screen
            composable(
                route = Screen.NewNote.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                NoteEditorScreen(
                    viewModel = entoucheViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSave = {
                        navController.popBackStack()
                        entoucheViewModel.loadNotes()
                        entoucheViewModel.loadStats()
                    }
                )
            }

            composable(
                route = Screen.NoteDetail.route,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.StringType }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                NoteDetailScreen(
                    noteId = noteId,
                    onNavigateBack = { navController.popBackStack() },
                    onEdit = { /* TODO: Navigate to editor */ }
                )
            }

            composable(
                route = Screen.Voice.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                VoiceCaptureScreen(
                    viewModel = entoucheViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSave = {
                        navController.popBackStack()
                        // Refresh notes list after saving
                        entoucheViewModel.loadNotes()
                        entoucheViewModel.loadStats()
                    }
                )
            }

            composable(
                route = Screen.Search.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                SearchScreen(
                    onNavigateToNoteDetail = { noteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                    },
                    modifier = Modifier.padding(bottom = if (showBottomNav) Dimensions.bottomNavHeight else Dimensions.spacingXs)
                )
            }

            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                SettingsScreen(
                    onSignOut = { authViewModel.signOut() },
                    userName = authState.userName,
                    userEmail = authState.userEmail,
                    modifier = Modifier.padding(bottom = if (showBottomNav) Dimensions.bottomNavHeight else Dimensions.spacingXs)
                )
            }

            // Memory Game Screen
            composable(
                route = Screen.MemoryGame.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                MemoryGameScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Bottom Navigation Bar
        AnimatedVisibility(
            visible = showBottomNav,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassBottomNavBar(
                selectedRoute = when {
                    currentRoute.startsWith("home") -> "home"
                    currentRoute.startsWith("notes") || currentRoute.startsWith("note/") -> "notes"
                    currentRoute.startsWith("voice") -> "voice"
                    currentRoute.startsWith("search") -> "search"
                    currentRoute.startsWith("settings") -> "settings"
                    else -> "home"
                },
                onItemSelected = { item ->
                    val route = when (item) {
                        NavItem.Home -> Screen.Home.route
                        NavItem.Notes -> Screen.Notes.route
                        NavItem.Voice -> Screen.Voice.route
                        NavItem.Search -> Screen.Search.route
                        NavItem.Settings -> Screen.Settings.route
                    }

                    navController.navigate(route) {
                        // Pop up to start destination to avoid building up a large stack
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
