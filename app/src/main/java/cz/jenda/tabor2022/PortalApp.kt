package cz.jenda.tabor2022

import android.app.Application
import androidx.room.Room
import cz.jenda.tabor2022.Constants.DbName
import cz.jenda.tabor2022.data.AppDatabase

class PortalApp : Application() {
    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DbName).build()
    }

    companion object {
        lateinit var instance: PortalApp
            private set
    }
}