package it.uniboft.aquarium.uicompose.screens.scanner


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.ApparatoConfig
import it.uniboft.aquarium.domain.usecases.SaveDeviceConfigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        if (_uiState.value is ScannerUiState.Success) return

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
            _uiState.update { ScannerUiState.Error("QR Code non compatibile.") }
        } catch (e: Exception) {
            _uiState.update { ScannerUiState.Error("Errore: ${e.message}") }
        }
    }


    fun resetScanner() {
        _uiState.update { ScannerUiState.Idle }
    }
}
