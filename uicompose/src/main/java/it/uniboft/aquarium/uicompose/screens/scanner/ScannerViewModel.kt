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


data class ScannerUiState(
    val isScanSuccessful: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val saveDeviceConfigUseCase: SaveDeviceConfigUseCase
) : ViewModel() {


    companion object {
        private const val KEY_ID_APPARATO = "idApparato"
        private const val KEY_TOTP_SECRET = "totpSecret"
    }


    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()


    fun onQrCodeScanned(rawValue: String) {
        if (_uiState.value.isScanSuccessful) return


        viewModelScope.launch {
            try {
                val json = JSONObject(rawValue)
                val config = ApparatoConfig(
                    idApparato = json.getString(KEY_ID_APPARATO),
                    totpSecret = json.getString(KEY_TOTP_SECRET)
                )


                saveDeviceConfigUseCase.execute(config)
                    .onSuccess {
                        _uiState.update { it.copy(isScanSuccessful = true, errorMessage = null) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(errorMessage = "Errore di salvataggio: ${e.message}") }
                    }


            } catch (e: JSONException) {
                _uiState.update { it.copy(errorMessage = "QR Code non compatibile.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Errore imprevisto: ${e.message}") }
            }
        }
    }


    fun resetError() {
        _uiState.update { it.copy(errorMessage = null) }
    }


    fun navigationHandled() {
        _uiState.update { it.copy(isScanSuccessful = false) }
    }
}
