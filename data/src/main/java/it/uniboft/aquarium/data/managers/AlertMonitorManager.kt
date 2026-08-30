package it.uniboft.aquarium.data.managers


import it.uniboft.aquarium.data.local.dao.AlertDao
import it.uniboft.aquarium.data.local.entities.AlertEntity
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds


@Singleton
class AlertMonitorManager @Inject constructor(
    private val wotRepository: IWotRepository,
    private val alertDao: AlertDao
) {
    // Scope slegato dalla UI, vive quanto il processo dell'applicazione
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isMonitoring = false


    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        monitorScope.launch {
            while (isActive) {
                checkAndSaveAlerts()
                delay(2500.milliseconds)
            }
        }
    }


    private suspend fun checkAndSaveAlerts() {
        val result = wotRepository.fetchWaterQuality()
        if (result.isSuccess) {
            val data = result.getOrNull()!!

            // Simula una scrittura se i valori superano le soglie
            if (data.ph < 6.5 || data.ph > 7.5) {
                saveAlert("pH", data.ph, "Valore pH anomalo rilevato")
            }
            if (data.temperature < 24.0 || data.temperature > 26.0) {
                saveAlert("Temperatura", data.temperature, "Temperatura fuori range")
            }
        }
    }


    private suspend fun saveAlert(param: String, value: Double, message: String) {
        val alert = AlertEntity(
            timestamp = System.currentTimeMillis(),
            parameter = param,
            value = value,
            severity = "warning",
            message = message
        )
        // La transaction inserisce e mantiene solo gli ultimi 50 record
        alertDao.insertAndTrim(alert)
    }
}
