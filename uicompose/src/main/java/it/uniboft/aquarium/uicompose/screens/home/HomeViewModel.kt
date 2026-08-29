package it.uniboft.aquarium.uicompose.screens.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


data class HomeUiState(
    val isLoading: Boolean = false,
    val waterQuality: WaterQuality? = null,
    val isPumpRunning: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wotRepository: IWotRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    private var pollingJob: Job? = null


    init {
        startPolling()
    }


    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchWaterQuality()
                delay(10.seconds)
            }
        }
    }


    fun stopPolling() {
        pollingJob?.cancel()
    }


    fun fetchWaterQuality() {
        viewModelScope.launch {
            if (_uiState.value.waterQuality == null) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            wotRepository.fetchWaterQuality().fold(
                onSuccess = { data ->
                    _uiState.update { it.copy(isLoading = false, waterQuality = data) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }


    fun togglePump(isRunning: Boolean) {
        viewModelScope.launch {
            wotRepository.updatePumpState(isRunning).fold(
                onSuccess = {
                    _uiState.update { it.copy(isPumpRunning = isRunning) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = "Comando fallito: ${error.localizedMessage}") }
                }
            )
        }
    }


    fun errorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
