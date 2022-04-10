package com.skylex_chess_clock.chessclock.util

import java.util.concurrent.TimeUnit

class TopLevelFiles {
    companion object {
        val clockTimes = listOf(
                TimeHelper(30, TimeUnit.SECONDS),
                TimeHelper(45, TimeUnit.SECONDS),
                TimeHelper(1, TimeUnit.MINUTES),
                TimeHelper(2, TimeUnit.MINUTES),
                TimeHelper(3, TimeUnit.MINUTES),
                TimeHelper(4, TimeUnit.MINUTES),
                TimeHelper(5, TimeUnit.MINUTES),
                TimeHelper(6, TimeUnit.MINUTES),
                TimeHelper(7, TimeUnit.MINUTES),
                TimeHelper(8, TimeUnit.MINUTES),
                TimeHelper(10, TimeUnit.MINUTES),
                TimeHelper(15, TimeUnit.MINUTES),
                TimeHelper(20, TimeUnit.MINUTES),
                TimeHelper(25, TimeUnit.MINUTES),
                TimeHelper(30, TimeUnit.MINUTES),
                TimeHelper(45, TimeUnit.MINUTES),
                TimeHelper(60, TimeUnit.MINUTES),
                TimeHelper(90, TimeUnit.MINUTES),
                TimeHelper(120, TimeUnit.MINUTES),
                TimeHelper(150, TimeUnit.MINUTES),
                TimeHelper(180, TimeUnit.MINUTES)
        )

        val incrementTimes = listOf(
                TimeHelper(0, TimeUnit.SECONDS),
                TimeHelper(1, TimeUnit.SECONDS),
                TimeHelper(2, TimeUnit.SECONDS),
                TimeHelper(3, TimeUnit.SECONDS),
                TimeHelper(4, TimeUnit.SECONDS),
                TimeHelper(5, TimeUnit.SECONDS),
                TimeHelper(7, TimeUnit.SECONDS),
                TimeHelper(8, TimeUnit.SECONDS),
                TimeHelper(9, TimeUnit.SECONDS),
                TimeHelper(10, TimeUnit.SECONDS),
                TimeHelper(15, TimeUnit.SECONDS),
                TimeHelper(20, TimeUnit.SECONDS),
                TimeHelper(25, TimeUnit.SECONDS),
                TimeHelper(30, TimeUnit.SECONDS),
                TimeHelper(45, TimeUnit.SECONDS),
                TimeHelper(60, TimeUnit.SECONDS),
                TimeHelper(90, TimeUnit.SECONDS),
                TimeHelper(120, TimeUnit.SECONDS),
                TimeHelper(150, TimeUnit.SECONDS),
                TimeHelper(180, TimeUnit.SECONDS)
        )

        enum class ClockMode(name: String) {
            SUDDEN_DEATH("Sudden Death"),
            INCREMENT("Increment"),
            SIMPLE_DELAY("Simple Delay"),
            HOURGLASS("Hourglass"),
            UNDEFINED("");

            override fun toString(): String {
                return name
            }
            companion object {
                fun getAllModes(): List<ClockMode> {
                    return listOf(SUDDEN_DEATH, INCREMENT, SIMPLE_DELAY, HOURGLASS)
                }
                fun getClockModeFromName(name: String): ClockMode {
                    getAllModes().forEach {
                        if (it.name == name) {
                            return it
                        }
                    }
                    return UNDEFINED
                }
            }
        }
    }
}