package it.uniboft.aquarium.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import it.uniboft.aquarium.data.local.dao.WotDao
import it.uniboft.aquarium.data.local.entities.WaterQualityEntity

@Database(entities = [WaterQualityEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wotDao(): WotDao
}
