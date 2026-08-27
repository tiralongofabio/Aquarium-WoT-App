package it.uniboft.aquarium.data.repositories


import it.uniboft.aquarium.data.remote.api.PumpCommandDto
import it.uniboft.aquarium.data.remote.api.WotHttpApi
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.IWotRepository
import javax.inject.Inject


class HttpRepositoryImpl @Inject constructor(
    private val api: WotHttpApi
) : IWotRepository {


    override suspend fun fetchWaterQuality(): Result<WaterQuality> {
        return try {
            val response = api.getWaterQuality()

            if (response.isSuccessful) {
                val dto = response.body() ?: throw Exception("Body nullo nella risposta HTTP")

                val waterQuality = WaterQuality(
                    timestamp = dto.timestamp,
                    ph = dto.ph,
                    orp = dto.orp,
                    temperature = dto.temperature
                )

                Result.success(waterQuality)
            } else {
                Result.failure(Exception("Errore HTTP: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updatePumpState(isRunning: Boolean): Result<Unit> {
        return try {
            val command = PumpCommandDto(isRunning = isRunning)
            val response = api.setPumpState(command)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Errore HTTP: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
