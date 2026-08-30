package it.uniboft.aquarium.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import it.uniboft.aquarium.data.local.dao.AlertDao
import it.uniboft.aquarium.data.local.entities.AlertEntity
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import android.content.pm.ServiceInfo



@AndroidEntryPoint
class AquariumMonitorService : Service() {


    @Inject lateinit var wotRepository: IWotRepository
    @Inject lateinit var alertDao: AlertDao


    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isPolling = false


    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Android 14+ richiede la specifica del tipo a runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                createForegroundNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(1, createForegroundNotification())
        }
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isPolling) {
            isPolling = true
            serviceScope.launch {
                while (isActive) {
                    checkAnomalies()
                    delay(2500.milliseconds) // Polling ogni 2.5 secondi
                }
            }
        }
        return START_STICKY
    }


    private suspend fun checkAnomalies() {
        val result = wotRepository.fetchWaterQuality()
        if (result.isSuccess) {
            val data = result.getOrNull()!!

            // Logica semplificata: da sostituire con la lettura dei Range Configurati
            if (data.ph < 6.5 || data.ph > 7.5) {
                registerAndNotify("pH Anomalo", "Valore attuale: ${data.ph}", data.ph)
            }
        }
    }


    private suspend fun registerAndNotify(param: String, msg: String, value: Double) {
        val alert = AlertEntity(
            timestamp = System.currentTimeMillis(),
            parameter = param,
            value = value,
            severity = "alert",
            message = msg
        )
        // Salva e mantiene max 50 record
        alertDao.insertAndTrim(alert)


        // Genera notifica Push (Throttle necessario in prod per non spammare l'utente ogni 2.5s)
        val notification = NotificationCompat.Builder(this, "CRITICAL_ALERTS")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Allarme Acquario: $param")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()


        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }


    override fun onBind(intent: Intent?): IBinder? = null


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 31) {
            val manager = getSystemService(NotificationManager::class.java)
            // Canale per il servizio persistente (Silenzioso)
            val fgChannel = NotificationChannel("FG_SERVICE", "Monitoraggio Attivo", NotificationManager.IMPORTANCE_LOW)
            // Canale per gli allarmi critici (Alta priorità)
            val alertChannel = NotificationChannel("CRITICAL_ALERTS", "Allarmi Critici", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(fgChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }




    private fun createForegroundNotification() = NotificationCompat.Builder(this, "FG_SERVICE")
        .setContentTitle("Acquario WoT")
        .setContentText("Monitoraggio in background attivo")
        .setSmallIcon(android.R.drawable.ic_menu_info_details)
        .build()
}
