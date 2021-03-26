package com.skylex_chess_clock.chessclock.util

import android.os.Parcelable
import com.f2prateek.rx.preferences2.Preference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
data class TimeHelper(
        val time: Long? = null,
        val timeUnit: TimeUnit? = null
): Parcelable {
    override fun toString(): String {
        var timeInString = "$time"

        when (timeUnit) {
            TimeUnit.SECONDS -> {
                timeInString += " secs"
            }
            TimeUnit.MINUTES -> {
                timeInString += " mins"
            }
            TimeUnit.HOURS -> {
                timeInString += " hours"
            }
        }

        if (time == 1L) {
            timeInString = timeInString.substring(0, timeInString.length - 1)
        }

        return timeInString
    }

    override fun equals(other: Any?): Boolean {
        return (other is TimeHelper)
                && time == other.time
                && timeUnit == other.timeUnit
    }

    object TimeHelperPreferencesConverter: Preference.Converter<TimeHelper> {
        override fun deserialize(serialized: String): TimeHelper {
            val gson = Gson()
            val type = object : TypeToken<TimeHelper>() {}.type
            return gson.fromJson(serialized, type)
        }

        override fun serialize(value: TimeHelper): String {
            val gson = Gson()
            val type = object : TypeToken<TimeHelper>() {}.type
            return gson.toJson(value, type)
        }
    }
}