package it.uniboft.aquarium.data.repositories


import it.uniboft.aquarium.data.di.IoDispatcher
import it.uniboft.aquarium.data.remote.api.WotHttpApi
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
            val targetSpeed = if (isRunning) 100 else 0

            // Retrofit invia l'intero direttamente come payload JSON
            val response = api.setPumpSpeed(targetSpeed)

            if (!response.isSuccessful) {
                throw Exception("Errore di rete ${response.code()}: Impossibile comandare la pompa")
            }

            val body = response.body()
            if (body == null || !body.success) {
                // Intercetta un eventuale rifiuto logico da parte dell'orchestratore WoT
                throw Exception("Rifiutato dal dispositivo: ${body?.message ?: "Sconosciuto"}")
            }
        }
    }
}
