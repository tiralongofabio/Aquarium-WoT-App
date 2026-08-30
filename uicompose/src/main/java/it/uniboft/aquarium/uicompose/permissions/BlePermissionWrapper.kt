package it.uniboft.aquarium.uicompose.permissions


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner


@Composable
fun BlePermissionWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current


    // Determina i permessi corretti in base all'API level
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    var hasAllPermissions by remember {
        mutableStateOf(permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }


    var showRationale by remember {
        mutableStateOf(
            activity?.let { act ->
                permissionsToRequest.any { ActivityCompat.shouldShowRequestPermissionRationale(act, it) }
            } ?: false
        )
    }


    var isRequestingPermission by remember { mutableStateOf(false) }
    var hasAttemptedRequest by remember { mutableStateOf(false) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        hasAllPermissions = permissionsMap.values.all { it }
        showRationale = activity?.let { act ->
            permissionsToRequest.any { ActivityCompat.shouldShowRequestPermissionRationale(act, it) }
        } ?: false
        isRequestingPermission = false
        hasAttemptedRequest = true
    }


    // Osserva il ciclo di vita per aggiornare i permessi se cambiati dalle impostazioni OS
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAllPermissions = permissionsToRequest.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    // Lancia la richiesta automatica al primo avvio
    LaunchedEffect(hasAllPermissions) {
        if (!hasAllPermissions && !showRationale && !hasAttemptedRequest) {
            isRequestingPermission = true
            permissionLauncher.launch(permissionsToRequest)
        }
    }


    if (hasAllPermissions) {
        content() // Permessi ok: inietta il grafo di navigazione dell'app
    } else {
        // UI di blocco sicura (evita crash di navigazione)
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Permessi Bluetooth Necessari",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "L'app necessita del Bluetooth per connettersi all'acquario. Nessun dato di posizione verrà tracciato.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))


                when {
                    isRequestingPermission || (!hasAttemptedRequest && !showRationale) -> {
                        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = "Attesa autorizzazione" })
                    }
                    showRationale -> {
                        Button(
                            onClick = {
                                isRequestingPermission = true
                                permissionLauncher.launch(permissionsToRequest)
                            }
                        ) {
                            Text("Concedi Permessi")
                        }
                    }
                    else -> {
                        Button(onClick = { context.openAppSettings() }) {
                            Text("Apri Impostazioni")
                        }
                    }
                }
            }
        }
    }
}


private fun Context.openAppSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        startActivity(this)
    }
}
