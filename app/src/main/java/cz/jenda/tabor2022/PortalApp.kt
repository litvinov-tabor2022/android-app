package cz.jenda.tabor2022

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import cz.jenda.tabor2022.Constants.DbName
import cz.jenda.tabor2022.Constants.PreferencesName
import cz.jenda.tabor2022.data.AppDatabase
import java.io.File

class PortalApp : Application() {
    lateinit var dbPath: String
    lateinit var preferences: SharedPreferences

    val db: AppDatabase
        get() {
            return AppDatabase.getInstance(applicationContext)
        }

    fun populateDb(file: File) {
        db.close()
        AppDatabase.populateFromFile(applicationContext, file)
    }

    val sharedPrefs: SharedPreferences
        get() {
            return applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        }

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = getSharedPreferences(PreferencesName, MODE_PRIVATE)
        dbPath = getDatabasePath(DbName).absolutePath
    }

    companion object {
        lateinit var instance: PortalApp
            private set
    }
}