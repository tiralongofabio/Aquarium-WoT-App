package it.uniboft.aquarium.domain.models


data class WaterQuality(
    val temperature: Double,
    val ph: Double,
    val oxygenLevel: Double
) {
    companion object {
        // Valori di default richiesti finché il DB non si popola
        val Neutral = WaterQuality(temperature = 20.0, ph = 7.0, oxygenLevel = 8.0)
    }
}

