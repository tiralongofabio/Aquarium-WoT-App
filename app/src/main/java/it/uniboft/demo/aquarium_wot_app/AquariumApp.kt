package it.uniboft.demo.aquarium_wot_app


import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import it.uniboft.aquarium.data.managers.AlertMonitorManager
import javax.inject.Inject


@HiltAndroidApp
class AquariumApp : Application() {


    @Inject
    lateinit var alertMonitorManager: AlertMonitorManager


    override fun onCreate() {
        super.onCreate()
        // Avvia il monitoraggio silente al boot dell'app
        alertMonitorManager.startMonitoring()
    }
}
