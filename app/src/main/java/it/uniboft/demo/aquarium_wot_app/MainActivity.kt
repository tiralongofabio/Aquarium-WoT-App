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


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inietta il ViewModel associato all'Activity per controllarne lo stato
    private val splashViewModel: SplashViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Installa la splash nativa PRIMA di super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. Blocca la Splash Screen a schermo finché il ViewModel è in "Loading" (4 secondi)
        splashScreen.setKeepOnScreenCondition {
            splashViewModel.state.value is SplashState.Loading
        }

        enableEdgeToEdge()
        setContent {
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
