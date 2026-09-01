package it.uniboft.demo.aquarium_wot_app


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import it.uniboft.aquarium.uicompose.navigation.AppNavigation
import it.uniboft.aquarium.uicompose.permissions.BlePermissionWrapper
import it.uniboft.aquarium.data.utils.HardwareUtils
import it.uniboft.aquarium.uicompose.screens.splash.SplashState
import it.uniboft.aquarium.uicompose.screens.splash.SplashViewModel
import it.uniboft.aquarium.uicompose.ui.theme.AquariumTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AquariumTheme {
                if (HardwareUtils.isEmulator()) {
                    AppNavigation()
                } else {
                    BlePermissionWrapper {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

