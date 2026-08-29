package it.uniboft.aquarium.uicompose.navigation


sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object Scanner : Routes("scanner")
    data object Home : Routes("home")
    data object Settings : Routes("settings")
    data object Notifications : Routes("notifications")
    data object Configuration : Routes("configuration")
}
