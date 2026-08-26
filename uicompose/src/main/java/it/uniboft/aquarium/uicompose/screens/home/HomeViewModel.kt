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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

// Stato immutabile della UI
data class HomeUiState(
    val waterQuality: WaterQuality? = null,
    val isPumpRunning: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val localRepository: ILocalRepository,
    private val syncWotDataUseCase: SyncWotDataUseCase,
    private val updatePumpStateUseCase: UpdatePumpStateUseCase
) : ViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    private var pollingJob: Job? = null


    init {
        observeLocalDatabase()
        startPolling()
    }


    private fun observeLocalDatabase() {
        // Reagisce in tempo reale a qualsiasi modifica nel DB locale.
        localRepository.getWaterQualityStream()
            .onEach { data ->
                _uiState.update { it.copy(waterQuality = data) }
            }
            .launchIn(viewModelScope) // launchIn è thread-safe e vincolato al lifecycle del ViewModel
    }


    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                // Esegue il fetch. Se ha successo, l'Use Case aggiorna il DB locale.
                val result = syncWotDataUseCase.execute()


                result.onSuccess {
                    _uiState.update { it.copy(isOffline = false, errorMessage = null) }
                }

                result.onFailure { exception ->
                    // In caso di errore (es. rete assente), la UI va in offline mode,
                    // ma l'utente continuerà a vedere l'ultimo stato noto grazie a observeLocalDatabase()
                    _uiState.update { it.copy(isOffline = true, errorMessage = exception.message) }
                }


                delay(7000L) // Polling di 7 secondi
            }
        }
    }


    fun togglePump(isRunning: Boolean) {
        viewModelScope.launch {
            val result = updatePumpStateUseCase.execute(isRunning)

            result.onSuccess {
                _uiState.update { it.copy(isPumpRunning = isRunning, errorMessage = null) }
            }

            result.onFailure { exception ->
                _uiState.update { it.copy(errorMessage = "Errore comando: ${exception.message}") }
            }
        }
    }


    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}