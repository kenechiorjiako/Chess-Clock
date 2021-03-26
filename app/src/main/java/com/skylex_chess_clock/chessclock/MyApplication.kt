package com.skylex_chess_clock.chessclock

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
        lateinit var dataStore: DataStore<Preferences>
    }

    override fun onCreate() {
        super.onCreate()

        dataStore = applicationContext.createDataStore(name = PREFERENCE_KEY)
        instance = this
    }
}