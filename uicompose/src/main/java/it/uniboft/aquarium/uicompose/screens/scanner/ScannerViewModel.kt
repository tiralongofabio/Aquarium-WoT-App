package it.uniboft.aquarium.uicompose.screens.scanner


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.ApparatoConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


sealed class ScannerUiState {
    data object Idle : ScannerUiState()
    data class Success(val config: ApparatoConfig) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}


@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {
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
            _uiState.update { ScannerUiState.Success(config) }
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
