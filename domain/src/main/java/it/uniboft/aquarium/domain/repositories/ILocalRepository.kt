package it.uniboft.aquarium.domain.repositories


import it.uniboft.aquarium.domain.models.WaterQuality
import kotlinx.coroutines.flow.Flow
import it.uniboft.aquarium.domain.models.Alert



interface ILocalRepository {
    fun getWaterQualityStream(): Flow<WaterQuality>

    suspend fun saveWaterQuality(data: WaterQuality)

    fun getAlertsStream(): Flow<List<Alert>>
}

