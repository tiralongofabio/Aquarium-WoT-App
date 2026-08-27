package it.uniboft.aquarium.uicompose.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uniboft.aquarium.uicompose.screens.home.HomeScreen
import it.uniboft.aquarium.uicompose.screens.scanner.ScannerScreen
import it.uniboft.aquarium.uicompose.screens.scanner.ScannerViewModel


@Composable
fun AppNavigation() {
    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Routes.Scanner.route
    ) {
        composable(Routes.Splash.route) { /* Placeholder */ }

        composable(Routes.Scanner.route) {
            val viewModel: ScannerViewModel = viewModel()
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
            // Stessa logica per la Home, se non passavi il ViewModel prima
            HomeScreen(
                onNavigateToNotifications = {
                    navController.navigate(Routes.Notifications.route)
                }
            )
        }


        composable(Routes.Notifications.route) { /* Placeholder */ }
    }
}


