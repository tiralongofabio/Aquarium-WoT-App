package it.uniboft.aquarium.data.repositories


import it.uniboft.aquarium.data.di.IoDispatcher
import it.uniboft.aquarium.data.remote.api.WotHttpApi
import it.uniboft.aquarium.domain.models.PumpState
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class HttpRepositoryImpl @Inject constructor(
    private val api: WotHttpApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IWotRepository {


    override suspend fun fetchWaterQuality(): Result<WaterQuality> = withContext(ioDispatcher) {
        runCatching {
            val response = api.getWaterQuality()
            if (response.isSuccessful) {
                val data = response.body()
                WaterQuality(
                    temperature = data?.temperature ?: 0.0,
                    ph = data?.pH ?: 0.0,
                    oxygenLevel = data?.oxygenLevel ?: 0.0
                )
            } else {
                throw Exception("Errore HTTP ${response.code()}: Impossibile leggere i sensori")
            }
        }
    }


    override suspend fun updatePumpState(isRunning: Boolean): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            // Imposta fissa la velocità a 70% quando attivata dalla UI
            val targetSpeed = if (isRunning) 70 else 0
            val response = api.setPumpSpeed(targetSpeed)
            if (!response.isSuccessful) throw Exception("Errore HTTP: Impossibile comandare la pompa")
        }
    }

    override suspend fun fetchPumpState(): Result<PumpState> = withContext(ioDispatcher) {
        runCatching {
            val response = api.getPumpState()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw Exception("Payload della pompa vuoto")

                // Mapper esplicito: DTO (Data) -> Entity (Domain)
                PumpState(
                    isRunning = (dto.pumpSpeed ?: 0) > 0 && dto.filterStatus != "cleaning",
                    speed = dto.pumpSpeed ?: 0,
                    filterHealth = dto.filterHealth ?: 100.0,
                    isCleaning = dto.filterStatus == "cleaning"
                )
            } else {
                throw Exception("Errore lettura pompa: ${response.code()}")
            }
        }
    }



    override suspend fun startCleaningCycle(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val response = api.startCleaningCycle()
            if (!response.isSuccessful) throw Exception("Errore avvio pulizia")
        }
    }

}
