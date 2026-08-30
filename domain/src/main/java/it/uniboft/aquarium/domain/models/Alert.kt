package it.uniboft.aquarium.domain.models

data class Alert(
    val id: Int,
    val timestamp: Long,
    val parameter: String,
    val value: Double,
    val severity: String,
    val message: String
)
