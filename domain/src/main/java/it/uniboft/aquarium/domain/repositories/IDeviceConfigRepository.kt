package it.uniboft.aquarium.domain.repositories


import it.uniboft.aquarium.domain.models.ApparatoConfig


interface IDeviceConfigRepository {
    suspend fun saveConfig(config: ApparatoConfig): Result<Unit>
    suspend fun getConfig(): Result<ApparatoConfig?>
    suspend fun clearConfig(): Result<Unit>
}

