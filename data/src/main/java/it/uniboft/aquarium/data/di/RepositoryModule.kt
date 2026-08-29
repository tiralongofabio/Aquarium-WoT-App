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
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideLocalRepository(
        wotDao: WotDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ILocalRepository {
        return LocalRepositoryImpl(wotDao, ioDispatcher)
    }


    @Provides
    @Singleton
    fun provideWotRepository(api: WotHttpApi, @IoDispatcher ioDispatcher: CoroutineDispatcher): IWotRepository {
        return HttpRepositoryImpl(api, ioDispatcher)
    }


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
