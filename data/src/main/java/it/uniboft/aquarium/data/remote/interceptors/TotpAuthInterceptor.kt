package it.uniboft.aquarium.data.remote.interceptors


import it.uniboft.aquarium.domain.repositories.IDeviceConfigRepository
import it.uniboft.aquarium.domain.repositories.ITotpGenerator
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class TotpAuthInterceptor @Inject constructor(
    private val configRepository: IDeviceConfigRepository,
    private val totpGenerator: ITotpGenerator
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()


        // Esegue la chiamata suspend bloccando unicamente il thread di rete di OkHttp
        val config = runBlocking {
            configRepository.getConfig().getOrNull()
        }


        if (config != null && config.totpSecret.isNotBlank()) {
            val freshToken = totpGenerator.generateCurrentToken(config.totpSecret)
            requestBuilder.addHeader("Authorization", "Bearer $freshToken")
        }


        return chain.proceed(requestBuilder.build())
    }
}
