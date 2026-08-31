package it.uniboft.aquarium.data.remote.api


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT


@JsonClass(generateAdapter = true)
data class SensorPropertiesDto(
    @Json(name = "temperature") val temperature: Double?,
    @Json(name = "pH") val pH: Double?,
    @Json(name = "oxygenLevel") val oxygenLevel: Double?
)


@JsonClass(generateAdapter = true)
data class PumpPropertiesDto(
    @Json(name = "pumpSpeed") val pumpSpeed: Int?,
    @Json(name = "filterStatus") val filterStatus: String?,
    @Json(name = "filterHealth") val filterHealth: Double?
)


@JsonClass(generateAdapter = true)
data class PumpActionResponseDto(
    @Json(name = "success") val success: Boolean,
    @Json(name = "newSpeed") val newSpeed: Int?,
    @Json(name = "message") val message: String
)


@JsonClass(generateAdapter = true)
data class RangeBoundsDto(
    @Json(name = "min") val min: Double,
    @Json(name = "max") val max: Double
)


@JsonClass(generateAdapter = true)
data class ParameterConfigDto(
    @Json(name = "unit") val unit: String,
    @Json(name = "description") val description: String,
    @Json(name = "configurable") val configurable: RangeBoundsDto,
    @Json(name = "optimal") val optimal: RangeBoundsDto
)


@JsonClass(generateAdapter = true)
data class SensorConfigDto(
    @Json(name = "mode") val mode: String,
    @Json(name = "parameters") val parameters: Map<String, ParameterConfigDto>
)


interface WotHttpApi {
    @GET("waterqualitysensor/properties")
    suspend fun getWaterQuality(): Response<SensorPropertiesDto>


    @GET("filterpump/properties")
    suspend fun getPumpState(): Response<PumpPropertiesDto>


    @GET("waterqualitysensor/properties/config")
    suspend fun getSensorConfig(): Response<SensorConfigDto>


    @PUT("waterqualitysensor/properties/config")
    suspend fun updateSensorConfig(@Body config: SensorConfigDto): Response<Void>


    @POST("filterpump/actions/setPumpSpeed")
    suspend fun setPumpSpeed(@Body speed: Int): Response<PumpActionResponseDto>


    @POST("filterpump/actions/cleaningCycle")
    suspend fun startCleaningCycle(): Response<Void>
}
