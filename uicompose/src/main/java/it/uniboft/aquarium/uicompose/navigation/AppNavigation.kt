package it.uniboft.aquarium.uicompose.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
//import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uniboft.aquarium.uicompose.screens.home.HomeScreen
import it.uniboft.aquarium.uicompose.screens.scanner.ScannerScreen
import it.uniboft.aquarium.uicompose.screens.scanner.ScannerViewModel
import it.uniboft.aquarium.uicompose.screens.splash.SplashScreen
import it.uniboft.aquarium.uicompose.screens.splash.SplashViewModel

import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun AppNavigation() {
    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {

        composable(Routes.Splash.route) {
            //val viewModel: SplashViewModel = viewModel()
            val viewModel: SplashViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()


            SplashScreen(
                state = state,
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToScanner = {
                    navController.navigate(Routes.Scanner.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Routes.Scanner.route) {
            //val viewModel: ScannerViewModel = viewModel()
            val viewModel: ScannerViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()


            ScannerScreen(
                uiState = uiState,
                onQrCodeScanned = viewModel::onQrCodeScanned,
                onResetScanner = viewModel::resetScanner,
                onScanSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Scanner.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToNotifications = {
                    navController.navigate(Routes.Notifications.route)
                }
            )
        }


        composable(Routes.Notifications.route) {
            // Placeholder
        }
    }
}




