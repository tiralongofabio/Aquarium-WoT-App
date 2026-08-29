package it.uniboft.aquarium.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


data class SensorPropertiesDto(val temperature: Double?, val pH: Double?, val oxygenLevel: Double?)


// Aggiunto filterHealth
data class PumpPropertiesDto(val pumpSpeed: Int?, val filterStatus: String?, val filterHealth: Double?)
data class PumpActionResponseDto(val success: Boolean, val newSpeed: Int?, val message: String)


interface WotHttpApi {
    @GET("waterqualitysensor/properties")
    suspend fun getWaterQuality(): Response<SensorPropertiesDto>


    @GET("filterpump/properties")
    suspend fun getPumpState(): Response<PumpPropertiesDto>


    @POST("filterpump/actions/setPumpSpeed")
    suspend fun setPumpSpeed(@Body speed: Int): Response<PumpActionResponseDto>


    // Endpoint per la pulizia
    @POST("filterpump/actions/cleaningCycle")
    suspend fun startCleaningCycle(): Response<PumpActionResponseDto>
}
