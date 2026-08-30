package it.uniboft.aquarium.uicompose.screens.home


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.uniboft.aquarium.domain.models.WaterQuality
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }


    LifecycleResumeEffect(Unit) {
        viewModel.startPolling()
        onPauseOrDispose {
            viewModel.stopPolling()
        }
    }


    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.errorShown()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Acquario WoT") },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.semantics { contentDescription = "Impostazioni" }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::manualRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.waterQuality == WaterQuality.Neutral && uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics { contentDescription = "Caricamento sensori" }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Aggiunto scroll per layout landscape
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(visible = uiState.isConnectionUnstable) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Apparato WoT non disponibile",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    WaterQualitySensors(waterQuality = uiState.waterQuality)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    PumpControlCard(
                        isPumpRunning = uiState.isPumpRunning,
                        pumpSpeed = uiState.pumpSpeed,
                        onToggle = viewModel::togglePump
                    )
                    FilterHealthCard(
                        health = uiState.filterHealth,
                        isCleaning = uiState.isCleaning,
                        onStartCleaning = viewModel::startCleaning
                    )
                }
            }
        }
    }
}


@Composable
private fun WaterQualitySensors(waterQuality: WaterQuality) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SensorCard(
            title = "Temp",
            value = String.format(Locale.US, "%.1f °C", waterQuality.temperature),
            modifier = Modifier.weight(1f)
        )
        SensorCard(
            title = "pH",
            value = String.format(Locale.US, "%.2f", waterQuality.ph),
            modifier = Modifier.weight(1f)
        )
        SensorCard(
            title = "O2",
            value = String.format(Locale.US, "%.1f mg/L", waterQuality.oxygenLevel),
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun PumpControlCard(isPumpRunning: Boolean, pumpSpeed: Int, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPumpRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Pompa Filtro", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = if (isPumpRunning) "Attiva ($pumpSpeed%)" else "Spenta",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                checked = isPumpRunning,
                onCheckedChange = onToggle,
                modifier = Modifier.semantics {
                    contentDescription = "Interruttore pompa filtro"
                    role = Role.Switch
                }
            )
        }
    }
}


@Composable
private fun FilterHealthCard(health: Double, isCleaning: Boolean, onStartCleaning: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Salute Filtro: ${String.format(Locale.US, "%.0f", health)}%", style = MaterialTheme.typography.titleMedium)
            // Progress bar e bottone in linea (Row) per ottimizzare lo spazio orizzontale
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (health / 100).toFloat() },
                    modifier = Modifier.weight(1f),
                    color = if (health > 30) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = onStartCleaning,
                    enabled = !isCleaning
                ) {
                    Text(if (isCleaning) "Pulizia..." else "Avvia")
                }
            }
        }
    }
}


@Composable
private fun SensorCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
