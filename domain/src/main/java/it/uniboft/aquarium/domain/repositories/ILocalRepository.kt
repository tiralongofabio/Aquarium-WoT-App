package it.uniboft.aquarium.domain.repositories

import it.uniboft.aquarium.domain.models.WaterQuality
import kotlinx.coroutines.flow.Flow

interface ILocalRepository {
    fun getWaterQualityStream(): Flow<WaterQuality?>
    suspend fun saveWaterQuality(data: WaterQuality)
    // ... altri metodi per la pompa e i log
}
