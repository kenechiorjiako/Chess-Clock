package com.skylex_chess_clock.chessclock.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.skylex_chess_clock.chessclock.util.PreferenceKeys
import com.skylex_chess_clock.chessclock.util.TimeMapper
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val time: TimeMapper,
    val increment: TimeMapper,
    val clockMode: ClockMode
)

@Singleton
class UserPreferencesRepo @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            mapToPreferences(it)
        }

    private fun mapToPreferences(preferences: Preferences): UserPreferences {
        val currentTimePreference = preferences[PreferenceKeys.timeHelperPreferenceKey] ?: TimeMapper.TimeHelperPreferencesConverter.serialize(
            TimeMapper(5, TimeUnit.MINUTES)
        )
        val time = TimeMapper.TimeHelperPreferencesConverter.deserialize(currentTimePreference)


        val timeIncrementPreference = preferences[PreferenceKeys.timeIncrementPreferenceKey] ?: TimeMapper.TimeHelperPreferencesConverter.serialize(TimeMapper(2, TimeUnit.SECONDS))
        val timeIncrement = TimeMapper.TimeHelperPreferencesConverter.deserialize(timeIncrementPreference)

        val clockModePreference = preferences[PreferenceKeys.clockModePreferenceKey] ?: ClockMode.SUDDEN_DEATH.name
        val clockMode = ClockMode.getClockModeFromName(clockModePreference)

        return UserPreferences(time, timeIncrement, clockMode)
    }

    suspend fun updateUserSettings(clockMode: ClockMode, time: TimeMapper, increment: TimeMapper) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.timeHelperPreferenceKey] = TimeMapper.TimeHelperPreferencesConverter.serialize(time)
            preferences[PreferenceKeys.timeIncrementPreferenceKey] = TimeMapper.TimeHelperPreferencesConverter.serialize(increment)
            preferences[PreferenceKeys.clockModePreferenceKey] = clockMode.name
        }
    }

    suspend fun updateClockMode(clockMode: ClockMode) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.clockModePreferenceKey] = clockMode.name
        }
    }

    suspend fun updateClockTime(time: TimeMapper) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.timeHelperPreferenceKey] = TimeMapper.TimeHelperPreferencesConverter.serialize(time)
        }
    }

    suspend fun updateTimeIncrement(increment: TimeMapper) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.timeIncrementPreferenceKey] = TimeMapper.TimeHelperPreferencesConverter.serialize(increment)
        }
    }

    suspend fun fetchInitialPreferences() = mapToPreferences(dataStore.data.first().toPreferences())
}