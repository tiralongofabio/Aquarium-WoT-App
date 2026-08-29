package it.uniboft.aquarium.domain.models


data class WaterQuality(
    val temperature: Double,
    val ph: Double,
    val oxygenLevel: Double // Modificato da 'orp' a 'oxygenLevel' per matchare il backend
) {
    companion object {
        // Default neutro per la UI in assenza di connessione o DB
        val Neutral = WaterQuality(temperature = 0.0, ph = 0.0, oxygenLevel = 0.0)
    }
}

