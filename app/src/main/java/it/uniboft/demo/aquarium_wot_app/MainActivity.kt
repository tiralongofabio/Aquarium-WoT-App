package it.uniboft.demo.aquarium_wot_app


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import it.uniboft.aquarium.uicompose.navigation.AppNavigation
import it.uniboft.aquarium.uicompose.permissions.BlePermissionWrapper
import it.uniboft.aquarium.data.utils.HardwareUtils


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
