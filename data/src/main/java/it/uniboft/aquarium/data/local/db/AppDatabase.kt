package it.uniboft.aquarium.data.local.db


import androidx.room.Database
import androidx.room.RoomDatabase
import it.uniboft.aquarium.data.local.dao.AlertDao
import it.uniboft.aquarium.data.local.dao.WotDao
import it.uniboft.aquarium.data.local.entities.AlertEntity
import it.uniboft.aquarium.data.local.entities.WaterQualityEntity


@Database(
    entities = [WaterQualityEntity::class, AlertEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wotDao(): WotDao
    abstract fun alertDao(): AlertDao // <- Questa riga risolve l'errore "Unresolved reference"
}
