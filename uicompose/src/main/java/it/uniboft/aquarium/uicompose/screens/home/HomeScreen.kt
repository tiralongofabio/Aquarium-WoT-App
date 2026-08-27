package it.uniboft.aquarium.uicompose.screens.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import it.uniboft.aquarium.domain.models.WaterQuality



// --- STATEFUL SCREEN ---
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }


    // Gestione reattiva e sicura degli errori tramite Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }


    HomeContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onTogglePump = viewModel::togglePump,
        onNavigateToNotifications = onNavigateToNotifications
    )
}


// --- STATELESS SCREEN (Design & A11y) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onTogglePump: (Boolean) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Aquarium Control") },
                actions = {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.semantics {
                            contentDescription = "Apri notifiche e log"
                        }
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isOffline) {
                OfflineBanner()
            }


            WaterQualityCard(uiState.waterQuality)
            PumpControlCard(
                isPumpRunning = uiState.isPumpRunning,
                onTogglePump = onTogglePump,
                enabled = !uiState.isOffline // Sicurezza: disabilita comandi se offline
            )
        }
    }
}


@Composable
private fun OfflineBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Dispositivo remoto non raggiungibile. Dati non aggiornati.",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun WaterQualityCard(waterQuality: it.uniboft.aquarium.domain.models.WaterQuality?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                // A11y: Raggruppa la lettura dei dati per i lettori di schermo (TalkBack)
                contentDescription = if (waterQuality != null) {
                    "Stato acqua: pH ${waterQuality.ph}, ORP ${waterQuality.orp}, Temperatura ${waterQuality.temperature} gradi"
                } else {
                    "Dati qualità acqua non ancora disponibili"
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Qualità dell'Acqua",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))


            if (waterQuality != null) {
                DataRow(label = "pH", value = waterQuality.ph.toString())
                DataRow(label = "ORP", value = "${waterQuality.orp} mV")
                DataRow(label = "Temperatura", value = "${waterQuality.temperature} °C")
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}


@Composable
private fun PumpControlCard(isPumpRunning: Boolean, onTogglePump: (Boolean) -> Unit, enabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Pompa di Ricircolo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isPumpRunning) "Attiva" else "Spenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPumpRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            Switch(
                checked = isPumpRunning,
                onCheckedChange = onTogglePump,
                enabled = enabled,
                modifier = Modifier.semantics {
                    role = Role.Switch
                    stateDescription = if (isPumpRunning) "Accesa" else "Spenta"
                }
            )
        }
    }
}


@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}


//preview
@Preview(showBackground = true, name = "Home - Dati OK")
@Composable
fun HomeContentPreview() {
    MaterialTheme {
        HomeContent(
            uiState = HomeUiState(
                waterQuality = WaterQuality(timestamp = 0L, ph = 7.2, orp = 300.0, temperature = 25.5),
                isPumpRunning = true,
                isOffline = false,
                errorMessage = null
            ),
            snackbarHostState = SnackbarHostState(),
            onTogglePump = {},
            onNavigateToNotifications = {}
        )
    }
}


@Preview(showBackground = true, name = "Home - Offline Mode")
@Composable
fun HomeContentOfflinePreview() {
    MaterialTheme {
        HomeContent(
            uiState = HomeUiState(
                waterQuality = WaterQuality(timestamp = 0L, ph = 6.8, orp = 250.0, temperature = 24.0),
                isPumpRunning = false,
                isOffline = true,
                errorMessage = "Connessione persa"
            ),
            snackbarHostState = SnackbarHostState(),
            onTogglePump = {},
            onNavigateToNotifications = {}
        )
    }
}
