package it.uniboft.aquarium.domain.repositories


interface ITotpGenerator {
    /**
     * Genera un token numerico a 6 cifre basato sul timestamp corrente.
     */
    fun generateCurrentToken(secretBase32: String): String
}
