package it.uniboft.aquarium.data.remote.api


import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


// DTO per il parsing JSON della risposta dal nodo WoT
data class WaterQualityDto(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("ph") val ph: Double,
    @SerializedName("orp") val orp: Double,
    @SerializedName("temperature") val temperature: Double
)


// DTO per inviare i comandi (es. accensione pompa)
data class PumpCommandDto(
    @SerializedName("isRunning") val isRunning: Boolean
)


interface WotHttpApi {
    // L'header Authorization viene iniettato a livello di client OkHttp tramite un Interceptor.
    // Questo mantiene l'interfaccia pulita e garantisce la freschezza del token TOTP.


    @GET("api/properties/waterQuality")
    suspend fun getWaterQuality(): Response<WaterQualityDto>


    @POST("api/actions/pump")
    suspend fun setPumpState(
        @Body command: PumpCommandDto
    ): Response<Unit>
}



