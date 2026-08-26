package it.uniboft.aquarium.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.uniboft.aquarium.domain.models.WaterQuality

@Entity(tableName = "water_quality")
data class WaterQualityEntity(
    // Fissiamo l'ID a 1: ci interessa solo mantenere l'ultimo stato noto dell'acquario
    @PrimaryKey val id: Int = 1,
    val timestamp: Long,
    val ph: Double,
    val orp: Double,
    val temperature: Double
) {
    // Mapper verso il dominio
    fun toDomain() = WaterQuality(timestamp, ph, orp, temperature)
}
