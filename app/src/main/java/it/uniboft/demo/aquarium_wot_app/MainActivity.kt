package it.uniboft.demo.aquarium_wot_app


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import it.uniboft.aquarium.data.services.AquariumMonitorService
import it.uniboft.aquarium.uicompose.navigation.AppNavigation


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    // Launcher per la richiesta del permesso a runtime (Modern Android API)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startMonitorService()
        }
        // Nota UX/A11y: in un'app di produzione, se isGranted == false,
        // si mostra uno Snackbar per spiegare che il monitoraggio in background è disabilitato.
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        checkPermissionsAndStartService()


        setContent {
            AppNavigation()
        }
    }


    private fun checkPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startMonitorService()
                }
                else -> {
                    // Richiede il permesso all'avvio
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 o inferiore: il permesso è implicito nel Manifest
            startMonitorService()
        }
    }


    private fun startMonitorService() {
        val serviceIntent = Intent(this, AquariumMonitorService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}

