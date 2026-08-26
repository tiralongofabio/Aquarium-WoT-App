package it.uniboft.aquarium.domain.usecases

import it.uniboft.aquarium.domain.repositories.ILocalRepository
import it.uniboft.aquarium.domain.repositories.IWotRepository

class UpdatePumpStateUseCase(
    private val wotRepository: IWotRepository,
    private val localRepository: ILocalRepository
) {
    suspend fun execute(isRunning: Boolean): Result<Unit> {
        // 1. Invia il comando al nodo remoto
        val result = wotRepository.updatePumpState(isRunning)

        // 2. Regola d'oro: l'update locale avviene SOLO se la rete non genera eccezioni
        result.onSuccess {
            // NOTA: Aggiungi un metodo 'savePumpState(isRunning)' nel tuo ILocalRepository
            // localRepository.savePumpState(isRunning)
        }

        return result
    }
}
