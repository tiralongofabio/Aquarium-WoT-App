package it.uniboft.aquarium.data.repositories


import it.uniboft.aquarium.data.local.dao.WotDao
import it.uniboft.aquarium.data.local.entities.WaterQualityEntity
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.ILocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRepositoryImpl(
    private val wotDao: WotDao
) : ILocalRepository {

    override fun getWaterQualityStream(): Flow<WaterQuality?> {
        // Mappa lo stream reattivo di Room nel modello di dominio
        return wotDao.getWaterQualityStream().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveWaterQuality(data: WaterQuality) {
        val entity = WaterQualityEntity(
            timestamp = data.timestamp,
            ph = data.ph,
            orp = data.orp,
            temperature = data.temperature
        )
        wotDao.insertOrUpdate(entity)
    }
}
