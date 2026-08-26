package it.uniboft.aquarium.data.repositories

import it.uniboft.aquarium.data.remote.api.PumpCommandDto
import it.uniboft.aquarium.data.remote.api.WotHttpApi
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


class HttpRepositoryImpl(
    private val api: WotHttpApi
) : IWotRepository {


    // HARDCODED TEMPORANEO: In produzione questo token deriverà dal TOTP generato
    // e salvato in EncryptedSharedPreferences (Security Best Practice).
    private val currentToken = "Bearer TEMPORARY_TOKEN"


    override suspend fun fetchWaterQuality(): Result<WaterQuality> = withContext(Dispatchers.IO) {
        try {
            val response = api.getWaterQuality(currentToken)
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException("Payload vuoto")
                Result.success(
                    WaterQuality(
                        timestamp = body.timestamp,
                        ph = body.ph,
                        orp = body.orp,
                        temperature = body.temperature
                    )
                )
            } else {
                Result.failure(IOException("HTTP Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e) // Intercetta timeout, assenza di rete, errori DNS
        }
    }


    override suspend fun updatePumpState(isRunning: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.setPumpState(currentToken, PumpCommandDto(isRunning))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("HTTP Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
