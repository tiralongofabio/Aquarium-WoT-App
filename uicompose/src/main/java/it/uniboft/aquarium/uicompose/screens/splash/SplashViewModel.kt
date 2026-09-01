package it.uniboft.aquarium.uicompose.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.usecases.CheckDeviceConfigUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


sealed class SplashState {
    data object Loading : SplashState()
    data object NavigateToHome : SplashState()
    data object NavigateToScanner : SplashState()
}


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkDeviceConfigUseCase: CheckDeviceConfigUseCase
) : ViewModel() {


    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()


    init {
        checkInitialDestination()
    }


    private fun checkInitialDestination() {
        viewModelScope.launch {
            delay(4.seconds) // Modificato per soddisfare il requisito dei 4 secondi
            val isConfigured = checkDeviceConfigUseCase.execute()
            if (isConfigured) {
                _state.value = SplashState.NavigateToHome
            } else {
                _state.value = SplashState.NavigateToScanner
            }
        }
    }

}
