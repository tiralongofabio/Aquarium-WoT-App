package it.uniboft.aquarium.uicompose.screens.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.RangeBounds
import it.uniboft.aquarium.domain.models.SensorConfig
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ConfigurationUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val config: SensorConfig? = null,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)


@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val wotRepository: IWotRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(ConfigurationUiState())
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()


    init {
        loadConfig()
    }


    fun loadConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            wotRepository.getSensorConfig().fold(
                onSuccess = { config ->
                    _uiState.update { it.copy(isLoading = false, config = config) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }


    // Aggiorna lo stato locale mentre l'utente muove gli slider
    fun updateOptimalRange(paramKey: String, min: Double, max: Double) {
        val currentConfig = _uiState.value.config ?: return
        val updatedParameters = currentConfig.parameters.toMutableMap()
        val currentParam = updatedParameters[paramKey] ?: return


        updatedParameters[paramKey] = currentParam.copy(optimal = RangeBounds(min, max))

        _uiState.update {
            it.copy(config = currentConfig.copy(parameters = updatedParameters))
        }
    }


    fun updateMode(newMode: String) {
        val currentConfig = _uiState.value.config ?: return
        _uiState.update { it.copy(config = currentConfig.copy(mode = newMode)) }
    }


    fun saveConfig() {
        val currentConfig = _uiState.value.config ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }
            wotRepository.updateSensorConfig(currentConfig).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }


    fun errorShown() { _uiState.update { it.copy(errorMessage = null) } }
    fun successShown() { _uiState.update { it.copy(saveSuccess = false) } }
}
