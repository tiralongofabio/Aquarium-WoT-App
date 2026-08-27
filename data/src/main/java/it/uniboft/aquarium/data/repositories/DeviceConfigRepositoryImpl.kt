package it.uniboft.aquarium.data.repositories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import it.uniboft.aquarium.domain.models.ApparatoConfig
import it.uniboft.aquarium.domain.repositories.IDeviceConfigRepository
import javax.inject.Inject


class DeviceConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
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


    override fun saveConfig(config: ApparatoConfig): Result<Unit> = runCatching {
        sharedPreferences.edit()
            .putString("ID_APPARATO", config.idApparato)
            .putString("TOTP_SECRET", config.totpSecret)
            .apply()
    }


    override fun getConfig(): Result<ApparatoConfig?> = runCatching {
        val id = sharedPreferences.getString("ID_APPARATO", null)
        val secret = sharedPreferences.getString("TOTP_SECRET", null)


        if (id != null && secret != null) {
            ApparatoConfig(id, secret)
        } else {
            null
        }
    }


    override fun clearConfig(): Result<Unit> = runCatching {
        sharedPreferences.edit().clear().apply()
    }
}
