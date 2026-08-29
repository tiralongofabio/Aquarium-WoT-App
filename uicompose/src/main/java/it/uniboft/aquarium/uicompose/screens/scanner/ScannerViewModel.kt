package it.uniboft.aquarium.uicompose.screens.scanner


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.ApparatoConfig
import it.uniboft.aquarium.domain.usecases.SaveDeviceConfigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


sealed class ScannerUiState {
    data object Idle : ScannerUiState()
    data object Success : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}


@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val saveDeviceConfigUseCase: SaveDeviceConfigUseCase
) : ViewModel() {


    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()


    fun onQrCodeScanned(rawValue: String) {
        // Previene elaborazioni multiple se il QR viene inquadrato ripetutamente
        if (_uiState.value is ScannerUiState.Success) return


        // Avvia la coroutine per gestire l'operazione di I/O (Main-Safe)
        viewModelScope.launch {
            try {
                val json = JSONObject(rawValue)
                val config = ApparatoConfig(
                    idApparato = json.getString("idApparato"),
                    totpSecret = json.getString("totpSecret")
                )


                saveDeviceConfigUseCase.execute(config)
                    .onSuccess {
                        _uiState.update { ScannerUiState.Success }
                    }
                    .onFailure { e ->
                        _uiState.update { ScannerUiState.Error("Errore di salvataggio: ${e.message}") }
                    }


            } catch (e: JSONException) {
                // Cattura errori di formato JSON mancante o chiavi errate
                _uiState.update { ScannerUiState.Error("QR Code non compatibile.") }
            } catch (e: Exception) {
                // Cattura eventuali eccezioni generiche, utilizzando il parametro 'e'
                _uiState.update { ScannerUiState.Error("Errore imprevisto: ${e.message}") }
            }
        }
    }


    fun resetScanner() {
        _uiState.update { ScannerUiState.Idle }
    }
}


