package it.uniboft.aquarium.domain.models

data class ApparatoConfig(
    val idApparato: String,
    val totpSecret: String // Ricevuto tramite QR Code
)
