package it.uniboft.aquarium.uicompose.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uniboft.aquarium.uicompose.screens.configuration.ConfigurationScreen
import it.uniboft.aquarium.uicompose.screens.home.HomeScreen
import it.uniboft.aquarium.uicompose.screens.notifications.NotificationsScreen
import it.uniboft.aquarium.uicompose.screens.scanner.ScannerScreen
import it.uniboft.aquarium.uicompose.screens.settings.SettingsScreen
import it.uniboft.aquarium.uicompose.screens.splash.SplashState
import it.uniboft.aquarium.uicompose.screens.splash.SplashViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            // BEST PRACTICE: Recuperiamo il ViewModel ancorato all'Activity
            // per mantenere lo stato sincronizzato con la dismiss della Splash Screen OS.
            val context = LocalContext.current
            val viewModel: SplashViewModel = hiltViewModel(context as ComponentActivity)

            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(state) {
                when (state) {
                    is SplashState.NavigateToHome -> {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                    is SplashState.NavigateToScanner -> {
                        navController.navigate(Routes.Scanner.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                    SplashState.Loading -> {
                        // Nessuna UI renderizzata: il sistema operativo sta coprendo l'app
                        // con la windowSplashScreenAnimatedIcon nativa per i 4 secondi configurati.
                    }
                }
            }
        }

        composable(Routes.Scanner.route) {
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onScanSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Scanner.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) },
                onNavigateToConfiguration = { navController.navigate(Routes.Configuration.route) },
                onNavigateToScanner = { navController.navigate(Routes.Scanner.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Configuration.route) {
            ConfigurationScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

