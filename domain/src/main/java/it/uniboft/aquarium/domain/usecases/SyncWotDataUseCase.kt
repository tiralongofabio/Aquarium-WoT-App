package it.uniboft.aquarium.domain.usecases

import it.uniboft.aquarium.domain.repositories.ILocalRepository
import it.uniboft.aquarium.domain.repositories.IWotRepository

class SyncWotDataUseCase(
    private val wotRepository: IWotRepository,
    private val localRepository: ILocalRepository
) {
    suspend fun execute(): Result<Unit> {
        // Tenta il recupero dal nodo IoT
        val result = wotRepository.fetchWaterQuality()

        // Se il recupero ha successo, aggiorna il DB locale silente
        result.onSuccess { data ->
            localRepository.saveWaterQuality(data)
        }

        // Restituisce l'esito (successo o fallimento) al ViewModel
        return result.map { }
    }
}
