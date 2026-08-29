package it.uniboft.aquarium.domain.usecases


import it.uniboft.aquarium.domain.models.ApparatoConfig
import it.uniboft.aquarium.domain.repositories.IDeviceConfigRepository
import javax.inject.Inject


class SaveDeviceConfigUseCase @Inject constructor(
    private val repository: IDeviceConfigRepository
) {
    suspend fun execute(config: ApparatoConfig): Result<Unit> {
        if (config.idApparato.isBlank() || config.totpSecret.isBlank()) {
            return Result.failure(IllegalArgumentException("Parametri di configurazione non validi"))
        }
        return repository.saveConfig(config)
    }
}
