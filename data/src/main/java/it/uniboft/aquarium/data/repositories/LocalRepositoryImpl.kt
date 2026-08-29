package it.uniboft.aquarium.data.repositories


import it.uniboft.aquarium.data.di.IoDispatcher
import it.uniboft.aquarium.data.local.dao.WotDao
import it.uniboft.aquarium.data.local.entities.WaterQualityEntity
import it.uniboft.aquarium.domain.models.WaterQuality
import it.uniboft.aquarium.domain.repositories.ILocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject


class LocalRepositoryImpl @Inject constructor(
    private val wotDao: WotDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ILocalRepository {


    override fun getWaterQualityStream(): Flow<WaterQuality> {
        return wotDao.getWaterQualityStream().map { entity ->
            // Se l'entità è null (DB vuoto all'avvio), restituisce il Neutral
            entity?.toDomain() ?: WaterQuality.Neutral
        }
    }


    override suspend fun saveWaterQuality(data: WaterQuality) = withContext(ioDispatcher) {
        val entity = WaterQualityEntity(
            id = 1,
            timestamp = System.currentTimeMillis(),
            ph = data.ph,
            oxygenLevel = data.oxygenLevel,
            temperature = data.temperature
        )
        wotDao.insertOrUpdate(entity)
    }
}
