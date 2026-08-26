package it.uniboft.aquarium.domain.models

data class WaterQuality(
    val timestamp: Long,
    val ph: Double,
    val orp: Double,
    val temperature: Double
)
