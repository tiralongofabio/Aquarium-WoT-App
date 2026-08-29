package it.uniboft.aquarium.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.uniboft.aquarium.data.local.entities.WaterQualityEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface WotDao {
    // Il tipo di ritorno DEVE essere nullable (WaterQualityEntity?)
    @Query("SELECT * FROM water_quality WHERE id = 1")
    fun getWaterQualityStream(): Flow<WaterQualityEntity?>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(data: WaterQualityEntity)
}
