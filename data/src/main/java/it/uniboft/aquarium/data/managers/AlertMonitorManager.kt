package it.uniboft.aquarium.data.managers


import it.uniboft.aquarium.data.local.dao.AlertDao
import it.uniboft.aquarium.data.local.entities.AlertEntity
import it.uniboft.aquarium.domain.models.RangeBounds
import it.uniboft.aquarium.domain.models.SensorConfig
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
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isMonitoring = false

    // Cache in memoria per i range, aggiornata periodicamente
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
        // Aggiorna la configurazione dal nodo WoT al primo avvio e poi ogni ~60 secondi (24 tick)
        if (cachedConfig == null || ticks % 24 == 0) {
            wotRepository.getSensorConfig().onSuccess { cachedConfig = it }
        }
        ticks++


        val config = cachedConfig ?: return // Salta il controllo se non abbiamo i range


        val result = wotRepository.fetchWaterQuality()
        if (result.isSuccess) {
            val data = result.getOrNull()!!

            // Normalizzazione a 1 cifra decimale in fase di rilevazione
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

        // Registra a DB SOLO se il livello è warning o critical
        if (severity != "ok") {
            val status = if (value < optimal.min) "sotto la soglia minima" else "sopra la soglia massima"
            val message = "Valore $name $status"

            val alert = AlertEntity(
                timestamp = System.currentTimeMillis(),
                parameter = name,
                value = value,
                severity = severity,
                message = message
            )
            alertDao.insertAndTrim(alert)
        }
    }


    /**
     * Stabilisce la severità in base allo scostamento percentuale rispetto al limite superato.
     * Soglia arbitraria: <= 5% -> warning, > 5% -> critical
     */
    private fun determineSeverity(value: Double, min: Double, max: Double): String {
        if (value in min..max) return "ok"

        val deviationPercentage = if (value < min) {
            (min - value) / min
        } else {
            (value - max) / max
        }

        return if (deviationPercentage > 0.05) "critical" else "warning"
    }


    // Ricerca robusta (case-insensitive) della metrica nel JSON del Server WoT
    private fun getOptimalRange(config: SensorConfig, keyMatch: String): RangeBounds? {
        return config.parameters.entries.firstOrNull {
            it.key.contains(keyMatch, ignoreCase = true)
        }?.value?.optimal
    }
}
