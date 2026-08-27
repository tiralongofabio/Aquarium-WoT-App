package it.uniboft.aquarium.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.uniboft.aquarium.data.remote.api.WotHttpApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    // 1. Configurazione client HTTP (Best practice: timeouts espliciti e logging)
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // TODO: Condizionare il livello di log solo alle build di DEBUG per sicurezza
            level = HttpLoggingInterceptor.Level.BODY
        }


        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            // Qui in futuro inietteremo l'interceptor per firmare le richieste col token TOTP
            .build()
    }


    // 2. Configurazione Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // Sostituisci con l'IP/Dominio reale dell'apparato WoT o del gateway
            .baseUrl("http://192.168.1.100:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    // 3. Risoluzione dell'errore Dagger (MissingBinding)
    @Provides
    @Singleton
    fun provideWotHttpApi(retrofit: Retrofit): WotHttpApi {
        return retrofit.create(WotHttpApi::class.java)
    }
}
