package it.uniboft.aquarium.uicompose.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToNotifications: () -> Unit,
    onNavigateToConfiguration: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Torna indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingsMenuItem(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Registro Notifiche",
                subtitle = "Storico degli allarmi e anomalie",
                onClick = onNavigateToNotifications
            )
            HorizontalDivider()
            SettingsMenuItem(
                icon = Icons.Default.Build,
                title = "Configurazione Range",
                subtitle = "Imposta i valori ottimali per pH, Temp e O2",
                onClick = onNavigateToConfiguration
            )
            HorizontalDivider()
            SettingsMenuItem(
                icon = Icons.Default.Add,
                title = "Aggiungi Dispositivo",
                subtitle = "Inquadra un nuovo QR-Code",
                onClick = onNavigateToScanner
            )
        }
    }
}


@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title, $subtitle"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
