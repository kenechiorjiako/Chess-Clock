package com.skylex_chess_clock.chessclock.util

import android.os.Parcelable
import com.f2prateek.rx.preferences2.Preference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
data class TimeMapper(
        val time: Long,
        val timeUnit: TimeUnit
): Parcelable {

    fun toSeconds(): Long {
        return timeUnit.toSeconds(time)
    }


    override fun toString(): String {
        var timeInString = "$time"

        timeInString += when (timeUnit) {
            TimeUnit.SECONDS -> " secs"
            TimeUnit.MINUTES -> " mins"
            TimeUnit.HOURS -> " hours"
            TimeUnit.NANOSECONDS -> " nano secs"
            TimeUnit.MICROSECONDS -> " micro secs"
            TimeUnit.MILLISECONDS -> " millis"
            TimeUnit.DAYS -> " days"
        }

        if (time == 1L) {
            timeInString = timeInString.substring(0, timeInString.length - 1)
        }

        return timeInString
    }

    override fun equals(other: Any?): Boolean {
        return (other is TimeMapper)
                && time == other.time
                && timeUnit == other.timeUnit
    }

    object TimeHelperPreferencesConverter: Preference.Converter<TimeMapper> {
        override fun deserialize(serialized: String): TimeMapper {
            val gson = Gson()
            val type = object : TypeToken<TimeMapper>() {}.type
            return gson.fromJson(serialized, type)
        }

        override fun serialize(value: TimeMapper): String {
            val gson = Gson()
            val type = object : TypeToken<TimeMapper>() {}.type
            return gson.toJson(value, type)
        }
    }
}