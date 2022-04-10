package com.skylex_chess_clock.chessclock.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import com.skylex_chess_clock.chessclock.PREFERENCE_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Singleton
    @Provides
    fun providesDataSource(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.createDataStore(name = PREFERENCE_KEY)
    }
}