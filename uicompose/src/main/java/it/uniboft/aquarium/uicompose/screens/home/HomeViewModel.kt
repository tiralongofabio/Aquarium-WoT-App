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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope



data class HomeUiState(
    val isLoading: Boolean = false,
    val waterQuality: WaterQuality = WaterQuality.Neutral,
    val isPumpRunning: Boolean = false,
    val pumpSpeed: Int = 0,
    val filterHealth: Double = 100.0,
    val isCleaning: Boolean = false,
    val isOffline: Boolean = false, // Traccia lo stato di connessione continuo
    val errorMessage: String? = null
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncWotDataUseCase: SyncWotDataUseCase,
    private val updatePumpStateUseCase: UpdatePumpStateUseCase,
    private val wotRepository: IWotRepository,
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
                syncData(isSilent = true)
                delay(5.seconds)
            }
        }
    }


    fun stopPolling() { pollingJob?.cancel() }


    // Chiamato dalla UI quando l'utente fa Pull-to-Refresh
    fun manualRefresh() {
        viewModelScope.launch {
            syncData(isSilent = false)
        }
    }


    private suspend fun syncData(isSilent: Boolean) {
        if (!isSilent) {
            _internalState.update { it.copy(isLoading = true, errorMessage = null) }
        }


        // coroutineScope fornisce il contesto necessario per usare async in modo sicuro
        coroutineScope {
            val sensorDeferred = async { syncWotDataUseCase.execute() }
            val pumpDeferred = async { wotRepository.fetchPumpState() }


            val sensorResult = sensorDeferred.await()
            val pumpResult = pumpDeferred.await()


            // Usiamo fold innestato per aggirare ogni potenziale problema dell'IDE
            // con l'inferenza di tipo delle inline class (come kotlin.Result)
            sensorResult.fold(
                onSuccess = {
                    pumpResult.fold(
                        onSuccess = { pumpState ->
                            _internalState.update {
                                it.copy(
                                    isLoading = false,
                                    isOffline = false, // Reset stato offline
                                    isPumpRunning = pumpState.isRunning,
                                    pumpSpeed = pumpState.speed,
                                    filterHealth = pumpState.filterHealth,
                                    isCleaning = pumpState.isCleaning
                                )
                            }
                        },
                        onFailure = { handleOfflineState(isSilent) }
                    )
                },
                onFailure = { handleOfflineState(isSilent) }
            )
        }
    }


    private fun handleOfflineState(isSilent: Boolean) {
        val wasOffline = _internalState.value.isOffline
        _internalState.update {
            it.copy(
                isLoading = false,
                isOffline = true,
                // Mostra l'errore SOLO se l'utente ha forzato il refresh o se l'app è appena andata offline
                errorMessage = if (!wasOffline || !isSilent) "API WoT non disponibili o non raggiungibili." else null
            )
        }
    }



    fun togglePump(isRunning: Boolean) {
        _internalState.update { it.copy(isPumpRunning = isRunning, pumpSpeed = if (isRunning) 70 else 0) }
        viewModelScope.launch {
            updatePumpStateUseCase.execute(isRunning).onFailure { error ->
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


