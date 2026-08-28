package it.uniboft.aquarium.data.remote.interceptors


import it.uniboft.aquarium.domain.repositories.IDeviceConfigRepository
import it.uniboft.aquarium.domain.repositories.ITotpGenerator
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class TotpAuthInterceptor @Inject constructor(
    private val configRepository: IDeviceConfigRepository,
    private val totpGenerator: ITotpGenerator
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()


        // 1. Recupera il secret in modo sincrono dalle EncryptedSharedPreferences
        val config = configRepository.getConfig().getOrNull()


        if (config != null && config.totpSecret.isNotBlank()) {
            // 2. Genera il token fresco al momento esatto dell'invio
            val freshToken = totpGenerator.generateCurrentToken(config.totpSecret)

            // 3. Firma la richiesta secondo lo standard Bearer
            requestBuilder.addHeader("Authorization", "Bearer $freshToken")
        }


        // 4. Procede con l'esecuzione della chiamata di rete
        return chain.proceed(requestBuilder.build())
    }
}
