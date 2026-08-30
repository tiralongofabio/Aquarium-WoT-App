package it.uniboft.aquarium.uicompose.screens.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.ILocalRepository
import it.uniboft.aquarium.domain.repositories.IWotRepository
import it.uniboft.aquarium.domain.usecases.SyncWotDataUseCase
import it.uniboft.aquarium.domain.usecases.UpdatePumpStateUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val pumpSpeed: Int = 0,
    val filterHealth: Double = 100.0,
    val isCleaning: Boolean = false,
    val isConnectionUnstable: Boolean = false,
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

    // Single Source of Truth (SSOT): Il DB guida la UI per i sensori
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
    private var failedAttempts = 0


    // Lifecycle-aware: chiamato da HomeScreen quando OnTop
    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                syncData(isSilent = true)
                delay(5.seconds)
            }
        }
    }


    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }


    fun manualRefresh() {
        viewModelScope.launch {
            syncData(isSilent = false)
        }
    }


    private suspend fun syncData(isSilent: Boolean) {
        if (!isSilent) {
            _internalState.update { it.copy(isLoading = true, errorMessage = null) }
        }


        coroutineScope {
            val sensorDeferred = async { syncWotDataUseCase.execute() }
            val pumpDeferred = async { wotRepository.fetchPumpState() }

            val sensorResult = sensorDeferred.await()
            val pumpResult = pumpDeferred.await()


            // Best Practice: logica appiattita, nessun "nested fold"
            if (sensorResult.isSuccess && pumpResult.isSuccess) {
                val pumpState = pumpResult.getOrNull()!!
                failedAttempts = 0 // Reset fallimenti

                _internalState.update {
                    it.copy(
                        isLoading = false,
                        isConnectionUnstable = false,
                        isPumpRunning = pumpState.isRunning,
                        pumpSpeed = pumpState.speed,
                        filterHealth = pumpState.filterHealth,
                        isCleaning = pumpState.isCleaning
                    )
                }
            } else {
                handleOfflineState(isSilent)
            }
        }
    }


    private fun handleOfflineState(isSilent: Boolean) {
        failedAttempts++
        val isUnstable = failedAttempts >= 3

        _internalState.update {
            it.copy(
                isLoading = false,
                isConnectionUnstable = isUnstable,
                errorMessage = if (!isSilent && !isUnstable) "Tentativo fallito ($failedAttempts/3)..." else null
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


    fun errorShown() {
        _internalState.update { it.copy(errorMessage = null) }
    }
}
