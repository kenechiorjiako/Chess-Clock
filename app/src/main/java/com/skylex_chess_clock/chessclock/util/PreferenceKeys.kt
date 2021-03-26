package com.skylex_chess_clock.chessclock.util

import androidx.datastore.preferences.core.stringPreferencesKey
import com.skylex_chess_clock.chessclock.CLOCK_MODE_RESULT_KEY
import com.skylex_chess_clock.chessclock.TIME_HELPER_PREFERENCE_KEY
import com.skylex_chess_clock.chessclock.TIME_INCREMENT_PREFERENCE_KEY

class PreferenceKeys {
    companion object {
        val timeHelperPreferenceKey = stringPreferencesKey(TIME_HELPER_PREFERENCE_KEY)
        val timeIncrementPreferenceKey = stringPreferencesKey(TIME_INCREMENT_PREFERENCE_KEY)
        val clockModePreferenceKey = stringPreferencesKey(CLOCK_MODE_RESULT_KEY)
    }
}