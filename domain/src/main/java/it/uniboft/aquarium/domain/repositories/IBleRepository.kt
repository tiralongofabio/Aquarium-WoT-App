package it.uniboft.aquarium.domain.repositories

import kotlinx.coroutines.flow.StateFlow


interface IBleRepository {
    val connectionState: StateFlow<Boolean>
    fun startScan()
    fun disconnect()
}
