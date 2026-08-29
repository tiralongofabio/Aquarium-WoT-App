package it.uniboft.aquarium.domain.models

data class SensorConfig(
    val mode: String,
    val parameters: Map<String, ParameterConfig>
)


data class ParameterConfig(
    val unit: String,
    val description: String,
    val configurable: RangeBounds,
    val optimal: RangeBounds
)


data class RangeBounds(
    val min: Double,
    val max: Double
)
