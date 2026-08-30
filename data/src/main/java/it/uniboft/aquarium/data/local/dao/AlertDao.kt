package it.uniboft.aquarium.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import it.uniboft.aquarium.data.local.entities.AlertEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAlertsStream(): Flow<List<AlertEntity>>


    @Insert
    suspend fun insertAlert(alert: AlertEntity)


    @Query("DELETE FROM alerts WHERE id NOT IN (SELECT id FROM alerts ORDER BY timestamp DESC LIMIT 50)")
    suspend fun keepOnlyLast50()


    @Transaction
    suspend fun insertAndTrim(alert: AlertEntity) {
        insertAlert(alert)
        keepOnlyLast50()
    }
}
