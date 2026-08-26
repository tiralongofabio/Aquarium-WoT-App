package it.uniboft.aquarium.data.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.uniboft.aquarium.data.local.dao.WotDao
import it.uniboft.aquarium.data.remote.api.WotHttpApi
import it.uniboft.aquarium.data.repositories.HttpRepositoryImpl
import it.uniboft.aquarium.data.repositories.LocalRepositoryImpl
import it.uniboft.aquarium.domain.repositories.ILocalRepository
import it.uniboft.aquarium.domain.repositories.IWotRepository
import it.uniboft.aquarium.domain.usecases.SyncWotDataUseCase
import it.uniboft.aquarium.domain.usecases.UpdatePumpStateUseCase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideLocalRepository(wotDao: WotDao): ILocalRepository {
        return LocalRepositoryImpl(wotDao)
    }


    @Provides
    @Singleton
    fun provideWotRepository(api: WotHttpApi): IWotRepository {
        // Qui la logica del Flavor: per ora forniamo l'implementazione HTTP.
        // Quando implementeremo il BLE, faremo uno switch usando BuildConfig.FLAVOR
        return HttpRepositoryImpl(api)
    }


    // --- Provider per gli Use Cases ---

    @Provides
    fun provideSyncWotDataUseCase(
        wotRepository: IWotRepository,
        localRepository: ILocalRepository
    ): SyncWotDataUseCase {
        return SyncWotDataUseCase(wotRepository, localRepository)
    }


    @Provides
    fun provideUpdatePumpStateUseCase(
        wotRepository: IWotRepository,
        localRepository: ILocalRepository
    ): UpdatePumpStateUseCase {
        return UpdatePumpStateUseCase(wotRepository, localRepository)
    }
}
