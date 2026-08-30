package it.uniboft.aquarium.uicompose.screens.configuration


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(uiState.errorMessage, uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Configurazione salvata con successo", duration = SnackbarDuration.Short)
            viewModel.successShown()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.errorShown()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configurazione Range") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Torna indietro")
                    }
                },
                actions = {
                    if (uiState.config != null) {
                        IconButton(
                            onClick = viewModel::saveConfig,
                            enabled = !uiState.isSaving
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Salva configurazione")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.config != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ModeSelector(
                        currentMode = uiState.config!!.mode,
                        onModeSelected = viewModel::updateMode
                    )

                    HorizontalDivider()


                    uiState.config!!.parameters.forEach { (key, paramConfig) ->
                        ParameterSliderCard(
                            config = paramConfig,
                            onRangeChanged = { min, max -> viewModel.updateOptimalRange(key, min, max) }
                        )
                    }


                    OutlinedButton(
                        onClick = viewModel::resetToDefaults,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 24.dp)
                            .semantics { contentDescription = "Ripristina i valori di default di fabbrica" },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ripristina Valori di Default")
                    }
                }
            }


            if (uiState.isSaving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}


@Composable
private fun ModeSelector(currentMode: String, onModeSelected: (String) -> Unit) {
    Column {
        Text("Modalità di Sistema", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = currentMode == "demo",
                onClick = { onModeSelected("demo") },
                label = { Text("Demo") },
                modifier = Modifier.semantics { contentDescription = "Seleziona modalità demo" }
            )
            FilterChip(
                selected = currentMode == "production",
                onClick = { onModeSelected("production") },
                label = { Text("Produzione") },
                modifier = Modifier.semantics { contentDescription = "Seleziona modalità produzione" }
            )
        }
    }
}


@Composable
private fun ParameterSliderCard(
    config: it.uniboft.aquarium.domain.models.ParameterConfig,
    onRangeChanged: (Double, Double) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = config.description, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Range Ottimale: ${String.format(Locale.US, "%.1f", config.optimal.min)} - ${String.format(Locale.US, "%.1f", config.optimal.max)} ${config.unit}",
                style = MaterialTheme.typography.bodyMedium
            )


            RangeSlider(
                value = config.optimal.min.toFloat()..config.optimal.max.toFloat(),
                onValueChange = { range ->
                    val min = Math.round(range.start * 10.0) / 10.0
                    val max = Math.round(range.endInclusive * 10.0) / 10.0
                    onRangeChanged(min, max)
                },
                valueRange = config.configurable.min.toFloat()..config.configurable.max.toFloat(),
                modifier = Modifier.semantics {
                    contentDescription = "Regola range ottimale per ${config.description}"
                }
            )

            Text(
                text = "Limiti Hardware: ${config.configurable.min} - ${config.configurable.max} ${config.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
