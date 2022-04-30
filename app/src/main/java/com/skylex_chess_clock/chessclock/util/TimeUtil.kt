package com.skylex_chess_clock.chessclock.util

import java.util.*

class TimeUtil {
    companion object {
        fun getTimeTextFromSeconds(seconds: Long) : String {
            var timeText = ""

            val hours = seconds / 3600
            val minutes = seconds % 3600 / 60
            val secs = seconds % 60

            timeText = if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format(Locale.getDefault(), "%d:%02d", minutes, secs)
            }

            return timeText
        }
    }
}