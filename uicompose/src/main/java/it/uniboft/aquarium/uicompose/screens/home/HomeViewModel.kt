package it.uniboft.aquarium.domain.usecases


import it.uniboft.aquarium.domain.repositories.IDeviceConfigRepository
import javax.inject.Inject


class CheckDeviceConfigUseCase @Inject constructor(
    private val repository: IDeviceConfigRepository
) {
    // Aggiunto il modificatore 'suspend'
    suspend fun execute(): Boolean {
        // Ritorna true se la configurazione esiste ed è valida, false altrimenti
        return repository.getConfig().getOrNull() != null
    }
}
