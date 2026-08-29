package it.uniboft.aquarium.uicompose.screens.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.ILocalRepository
import it.uniboft.aquarium.domain.usecases.SyncWotDataUseCase
import it.uniboft.aquarium.domain.usecases.UpdatePumpStateUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


data class HomeUiState(
    val isLoading: Boolean = false,
    val waterQuality: WaterQuality = WaterQuality.Neutral,
    val isPumpRunning: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncWotDataUseCase: SyncWotDataUseCase,
    private val updatePumpStateUseCase: UpdatePumpStateUseCase,
    localRepository: ILocalRepository
) : ViewModel() {


    // Stato mutevole interno per gestire flag operativi (caricamento, errori, UI)
    private val _internalState = MutableStateFlow(HomeUiState())


    // Sintassi extension function: inferenza di tipo garantita dal compilatore
    val uiState: StateFlow<HomeUiState> = _internalState
        .combine(localRepository.getWaterQualityStream()) { state, localWaterQuality ->
            state.copy(waterQuality = localWaterQuality ?: WaterQuality.Neutral)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )





    private var pollingJob: Job? = null


    init {
        startPolling()
    }


    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                syncWaterQuality()
                delay(10.seconds)
            }
        }
    }


    fun stopPolling() {
        pollingJob?.cancel()
    }


    fun syncWaterQuality() {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true, errorMessage = null) }


            // L'Use Case scarica da HTTP e salva in Room.
            // La UI si aggiorna automaticamente grazie a getWaterQualityStream().
            syncWotDataUseCase.execute().fold(
                onSuccess = {
                    _internalState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _internalState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }


    fun togglePump(isRunning: Boolean) {
        viewModelScope.launch {
            updatePumpStateUseCase.execute(isRunning).fold(
                onSuccess = {
                    _internalState.update { it.copy(isPumpRunning = isRunning) }
                },
                onFailure = { error ->
                    _internalState.update { it.copy(errorMessage = "Comando fallito: ${error.localizedMessage}") }
                }
            )
        }
    }


    fun errorShown() {
        _internalState.update { it.copy(errorMessage = null) }
    }
}
