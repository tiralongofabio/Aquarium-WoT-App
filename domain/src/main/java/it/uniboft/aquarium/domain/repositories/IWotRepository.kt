package it.uniboft.aquarium.domain.repositories

import it.uniboft.aquarium.domain.models.WaterQuality

interface IWotRepository {
    suspend fun fetchWaterQuality(): Result<WaterQuality>
    suspend fun updatePumpState(isRunning: Boolean): Result<Unit>
}
