package it.uniboft.aquarium.domain.models

data class PumpState(
    val isRunning: Boolean,
    val speed: Int,
    val filterHealth: Double,
    val isCleaning: Boolean
)
