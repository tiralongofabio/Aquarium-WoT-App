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
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.async

data class HomeUiState(
    val isLoading: Boolean = false,
    val waterQuality: WaterQuality = WaterQuality.Neutral,
    val isPumpRunning: Boolean = false,
    val pumpSpeed: Int = 0,
    val filterHealth: Double = 100.0,
    val isCleaning: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncWotDataUseCase: SyncWotDataUseCase,
    private val updatePumpStateUseCase: UpdatePumpStateUseCase,
    private val wotRepository: IWotRepository, // Iniezione aggiunta
    localRepository: ILocalRepository
) : ViewModel() {


    private val _internalState = MutableStateFlow(HomeUiState())


    val uiState: StateFlow<HomeUiState> = _internalState
        .combine(localRepository.getWaterQualityStream()) { state, localWaterQuality ->
            state.copy(waterQuality = localWaterQuality ?: WaterQuality.Neutral)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())


    private var pollingJob: Job? = null


    init { startPolling() }


    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                syncWaterQualityAndPump()
                delay(5.seconds)
            }
        }
    }
    fun stopPolling() { pollingJob?.cancel() }


    fun syncWaterQualityAndPump() {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true, errorMessage = null) }


            // Esegue le due operazioni di rete in parallelo (best practice per performance)
            val sensorDeferred = async { syncWotDataUseCase.execute() }
            val pumpDeferred = async { wotRepository.fetchPumpState() }


            // Attende la conclusione di entrambe
            val sensorResult = sensorDeferred.await()
            val pumpResult = pumpDeferred.await()


            // Valuta il risultato della pompa (che guida la UI della card)
            pumpResult.fold(
                onSuccess = { pumpState ->
                    _internalState.update {
                        it.copy(
                            isLoading = false,
                            // Usa i nomi delle proprietà del modello di dominio puro (PumpState)
                            isPumpRunning = pumpState.isRunning,
                            pumpSpeed = pumpState.speed,
                            filterHealth = pumpState.filterHealth,
                            isCleaning = pumpState.isCleaning,
                            // Se la pompa va ma i sensori falliscono, propaga l'errore dei sensori
                            errorMessage = sensorResult.exceptionOrNull()?.localizedMessage
                        )
                    }
                },
                onFailure = { pumpError ->
                    _internalState.update {
                        it.copy(
                            isLoading = false,
                            // Priorità all'errore della pompa, ma se entrambi falliscono, uniamo i messaggi
                            errorMessage = sensorResult.exceptionOrNull()?.let { sensorError ->
                                "Errore Sensori: ${sensorError.localizedMessage}\nErrore Pompa: ${pumpError.localizedMessage}"
                            } ?: pumpError.localizedMessage
                        )
                    }
                }
            )
        }
    }



    fun togglePump(isRunning: Boolean) {
        // Aggiornamento ottimistico della UI
        _internalState.update { it.copy(isPumpRunning = isRunning, pumpSpeed = if (isRunning) 70 else 0) }
        viewModelScope.launch {
            updatePumpStateUseCase.execute(isRunning).onFailure { error ->
                // Rollback in caso di fallimento
                _internalState.update { it.copy(isPumpRunning = !isRunning, errorMessage = "Comando fallito: ${error.localizedMessage}") }
            }
        }
    }


    fun startCleaning() {
        _internalState.update { it.copy(isCleaning = true) }
        viewModelScope.launch {
            wotRepository.startCleaningCycle().onFailure { error ->
                _internalState.update { it.copy(isCleaning = false, errorMessage = error.localizedMessage) }
            }
        }
    }


    fun errorShown() { _internalState.update { it.copy(errorMessage = null) } }
}

