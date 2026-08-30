package it.uniboft.aquarium.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import it.uniboft.aquarium.domain.models.Alert


@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val parameter: String,
    val value: Double,
    val severity: String,
    val message: String
) {
    fun toDomain() = Alert(id, timestamp, parameter, value, severity, message)
}
