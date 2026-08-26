package it.uniboft.aquarium.uicompose.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun AppNavigation() {
    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            // Placeholder: Sostituiremo con SplashScreen
            // Quando finisce, chiama: navController.navigate(Routes.Scanner.route) { popUpTo(Routes.Splash.route) { inclusive = true } }
        }


        composable(Routes.Scanner.route) {
            // Placeholder: Sostituiremo con ScannerScreen
            // Quando il QR è valido, chiama: navController.navigate(Routes.Home.route)
        }


        composable(Routes.Home.route) {
            // Placeholder: Sostituiremo con HomeScreen
            // Gestisce la navigazione verso Notifiche
        }


        composable(Routes.Notifications.route) {
            // Placeholder: Sostituiremo con NotificationsScreen
        }
    }
}
