package it.uniboft.aquarium.domain.repositories

import it.uniboft.aquarium.domain.models.ApparatoConfig


interface IDeviceConfigRepository {
    fun saveConfig(config: ApparatoConfig): Result<Unit>
    fun getConfig(): Result<ApparatoConfig?>
    fun clearConfig(): Result<Unit>
}
