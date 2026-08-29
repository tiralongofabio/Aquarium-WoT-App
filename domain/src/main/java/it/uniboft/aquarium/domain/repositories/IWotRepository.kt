package it.uniboft.aquarium.domain.repositories

import it.uniboft.aquarium.domain.models.PumpState
import it.uniboft.aquarium.domain.models.SensorConfig
import it.uniboft.aquarium.domain.models.WaterQuality

interface IWotRepository {
    suspend fun fetchWaterQuality(): Result<WaterQuality>
    suspend fun updatePumpState(isRunning: Boolean): Result<Unit>
    suspend fun fetchPumpState(): Result<PumpState> // Nessuna dipendenza dal modulo 'data'
    suspend fun startCleaningCycle(): Result<Unit>

    suspend fun getSensorConfig(): Result<SensorConfig>

    suspend fun updateSensorConfig(config: SensorConfig): Result<Unit>

}

