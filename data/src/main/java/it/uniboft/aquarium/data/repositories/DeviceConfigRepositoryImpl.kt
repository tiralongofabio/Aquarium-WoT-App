package it.uniboft.aquarium.data.repositories


import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import it.uniboft.aquarium.data.di.IoDispatcher
import it.uniboft.aquarium.domain.models.ApparatoConfig
import it.uniboft.aquarium.domain.repositories.IDeviceConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class DeviceConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IDeviceConfigRepository {


    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()


    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_device_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )


    override suspend fun saveConfig(config: ApparatoConfig): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            sharedPreferences.edit()
                .putString("ID_APPARATO", config.idApparato)
                .putString("TOTP_SECRET", config.totpSecret)
                .apply() // .apply() esegue in background, ma la crittografia dei dati in memoria è sincrona e pesante
        }
    }


    override suspend fun getConfig(): Result<ApparatoConfig?> = withContext(ioDispatcher) {
        runCatching {
            val id = sharedPreferences.getString("ID_APPARATO", null)
            val secret = sharedPreferences.getString("TOTP_SECRET", null)


            if (id != null && secret != null) {
                ApparatoConfig(id, secret)
            } else {
                null
            }
        }
    }


    override suspend fun clearConfig(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            sharedPreferences.edit().clear().apply()
        }
    }
}
