package it.uniboft.aquarium.uicompose.screens.notifications


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.uniboft.aquarium.domain.models.Alert
import it.uniboft.aquarium.domain.repositories.ILocalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class NotificationsViewModel @Inject constructor(
    localRepository: ILocalRepository
) : ViewModel() {


    val alerts: StateFlow<List<Alert>> = localRepository.getAlertsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
