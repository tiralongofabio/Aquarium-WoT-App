package it.uniboft.aquarium.data.managers


import it.uniboft.aquarium.data.local.dao.AlertDao
import it.uniboft.aquarium.data.local.entities.AlertEntity
import it.uniboft.aquarium.data.utils.HardwareUtils
import it.uniboft.aquarium.domain.models.RangeBounds
import it.uniboft.aquarium.domain.models.SensorConfig
import it.uniboft.aquarium.domain.repositories.IWotRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import it.uniboft.aquarium.domain.repositories.IBleRepository



@Singleton
class AlertMonitorManager @Inject constructor(
    private val wotRepository: IWotRepository,
    private val alertDao: AlertDao,
    private val bleRepository: IBleRepository // Iniettato tramite interfaccia
) {
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isMonitoring = false

    private var cachedConfig: SensorConfig? = null
    private var ticks = 0


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
        // Prerequisito: Procedi con l'HTTP solo se su Emulatore o se il BLE fisico è connesso
        val isBleReady = HardwareUtils.isEmulator() || bleRepository.connectionState.value
        if (!isBleReady) return


        if (cachedConfig == null || ticks % 24 == 0) {
            wotRepository.getSensorConfig().onSuccess { cachedConfig = it }
        }
        ticks++


        val config = cachedConfig ?: return


        val result = wotRepository.fetchWaterQuality()
        if (result.isSuccess) {
            val data = result.getOrNull()!!

            val ph = Math.round(data.ph * 10.0) / 10.0
            val temp = Math.round(data.temperature * 10.0) / 10.0
            val o2 = Math.round(data.oxygenLevel * 10.0) / 10.0


            evaluateParameter("pH", ph, getOptimalRange(config, "ph"))
            evaluateParameter("Temperatura", temp, getOptimalRange(config, "temp"))
            evaluateParameter("Ossigeno", o2, getOptimalRange(config, "oxy") ?: getOptimalRange(config, "o2"))
        }
    }


    private suspend fun evaluateParameter(name: String, value: Double, optimal: RangeBounds?) {
        if (optimal == null) return
        val severity = determineSeverity(value, optimal.min, optimal.max)

        if (severity != "ok") {
            val status = if (value < optimal.min) "sotto la soglia minima" else "sopra la soglia massima"
            val alert = AlertEntity(
                timestamp = System.currentTimeMillis(),
                parameter = name,
                value = value,
                severity = severity,
                message = "Valore $name $status"
            )
            alertDao.insertAndTrim(alert)
        }
    }


    private fun determineSeverity(value: Double, min: Double, max: Double): String {
        if (value in min..max) return "ok"
        val deviationPercentage = if (value < min) (min - value) / min else (value - max) / max
        return if (deviationPercentage > 0.05) "critical" else "warning"
    }


    private fun getOptimalRange(config: SensorConfig, keyMatch: String): RangeBounds? {
        return config.parameters.entries.firstOrNull {
            it.key.contains(keyMatch, ignoreCase = true)
        }?.value?.optimal
    }
}
