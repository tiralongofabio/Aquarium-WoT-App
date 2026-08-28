package it.uniboft.aquarium.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.uniboft.aquarium.data.repositories.DeviceConfigRepositoryImpl
import it.uniboft.aquarium.domain.repositories.IDeviceConfigRepository
import javax.inject.Singleton
import it.uniboft.aquarium.data.repositories.TotpGeneratorImpl
import it.uniboft.aquarium.domain.repositories.ITotpGenerator



@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {


    @Binds
    @Singleton
    abstract fun bindDeviceConfigRepository(
        impl: DeviceConfigRepositoryImpl
    ): IDeviceConfigRepository

    @Binds
    @Singleton
    abstract fun bindTotpGenerator(
        impl: TotpGeneratorImpl
    ): ITotpGenerator

}
