package it.uniboft.aquarium.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.uniboft.aquarium.data.local.entities.WaterQualityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WotDao {
    // Restituisce uno stream reattivo. Ogni update nel DB farà emettere un nuovo valore al Flow.
    @Query("SELECT * FROM water_quality WHERE id = 1")
    fun getWaterQualityStream(): Flow<WaterQualityEntity?>

    // Sostituisce i vecchi dati con quelli nuovi in modo sincrono.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(data: WaterQualityEntity)
}
