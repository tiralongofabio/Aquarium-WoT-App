package it.uniboft.aquarium.data.di


import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.uniboft.aquarium.data.managers.BleConnectionManager
import it.uniboft.aquarium.domain.repositories.IBleRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {
    @Binds
    abstract fun bindBleRepository(impl: BleConnectionManager): IBleRepository
}
