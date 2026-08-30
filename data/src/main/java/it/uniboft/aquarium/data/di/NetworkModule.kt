package it.uniboft.aquarium.data.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.uniboft.aquarium.data.remote.api.WotHttpApi
import it.uniboft.aquarium.data.remote.interceptors.TotpAuthInterceptor
import it.uniboft.aquarium.data.utils.HardwareUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    fun provideOkHttpClient(totpAuthInterceptor: TotpAuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(totpAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }


    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // Routing della connessione HTTP in base all'hardware
        val baseUrl = if (HardwareUtils.isEmulator()) {
            "http://10.0.2.2:8080/"
        } else {
            // INSERISCI QUI L'IP LOCALE DEL TUO PC
            "http://192.168.1.66:8080/"
        }


        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    fun provideWotHttpApi(retrofit: Retrofit): WotHttpApi {
        return retrofit.create(WotHttpApi::class.java)
    }
}
