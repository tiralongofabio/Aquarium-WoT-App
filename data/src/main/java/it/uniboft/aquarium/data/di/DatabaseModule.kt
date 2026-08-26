package it.uniboft.aquarium.data.di


import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.uniboft.aquarium.data.local.dao.WotDao
import it.uniboft.aquarium.data.local.db.AppDatabase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aquarium_wot_db"
        ).fallbackToDestructiveMigration().build()
    }


    @Provides
    fun provideWotDao(database: AppDatabase): WotDao {
        return database.wotDao()
    }
}
