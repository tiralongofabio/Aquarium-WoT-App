package it.uniboft.aquarium.uicompose.screens.notifications


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.uniboft.aquarium.domain.models.Alert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Notifiche") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Torna indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nessuna notifica presente.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertItemCard(alert)
                }
            }
        }
    }
}


@Composable
private fun AlertItemCard(alert: Alert) { // Utilizzo pulito del modello di Dominio
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", LocalLocale.current.platformLocale)
    val dateString = formatter.format(Date(alert.timestamp))


    Card(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        colors = CardDefaults.cardColors(
            containerColor = if (alert.severity == "alert") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = if (alert.severity == "alert") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = alert.message, style = MaterialTheme.typography.titleMedium)
                Text(text = "$dateString • ${alert.parameter}: ${alert.value}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
