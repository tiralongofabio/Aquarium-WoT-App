package it.uniboft.aquarium.data.remote.api


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// DTO per il parsing JSON della risposta dal nodo WoT
data class WaterQualityDto(
    val timestamp: Long,
    val ph: Double,
    val orp: Double,
    val temperature: Double
)

// DTO per inviare i comandi (es. accensione pompa)
data class PumpCommandDto(
    val isRunning: Boolean
)

interface WotHttpApi {

    // Header Authorization per la sicurezza (passeremo il TOTP qui)
    @GET("api/properties/waterQuality")
    suspend fun getWaterQuality(
        @Header("Authorization") token: String
    ): Response<WaterQualityDto>

    @POST("api/actions/pump")
    suspend fun setPumpState(
        @Header("Authorization") token: String,
        @Body command: PumpCommandDto
    ): Response<Unit>
}
