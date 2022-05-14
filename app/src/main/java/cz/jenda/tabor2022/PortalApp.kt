package cz.jenda.tabor2022

import android.app.Application

class PortalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PortalApp
            private set
    }
}