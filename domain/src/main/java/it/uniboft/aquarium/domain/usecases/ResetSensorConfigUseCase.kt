package it.uniboft.aquarium.domain.usecases


import it.uniboft.aquarium.domain.models.RangeBounds
import it.uniboft.aquarium.domain.models.SensorConfig
import it.uniboft.aquarium.domain.repositories.IWotRepository
import javax.inject.Inject


class ResetSensorConfigUseCase @Inject constructor(
    private val repository: IWotRepository
) {
    suspend fun execute(): Result<SensorConfig> {
        return repository.getSensorConfig().mapCatching { currentConfig ->
            val updatedParameters = currentConfig.parameters.mapValues { (key, param) ->
                when {
                    key.contains("ph", ignoreCase = true) -> param.copy(optimal = RangeBounds(6.5, 7.5))
                    key.contains("temp", ignoreCase = true) -> param.copy(optimal = RangeBounds(24.0, 26.0))
                    key.contains("oxy", ignoreCase = true) || key.contains("o2", ignoreCase = true) -> param.copy(optimal = RangeBounds(6.0, 8.0))
                    else -> param
                }
            }


            val resetConfig = currentConfig.copy(
                mode = "demo",
                parameters = updatedParameters
            )


            repository.updateSensorConfig(resetConfig).getOrThrow()
            resetConfig
        }
    }
}
