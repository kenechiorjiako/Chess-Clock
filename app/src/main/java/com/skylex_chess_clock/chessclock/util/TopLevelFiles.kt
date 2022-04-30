package com.skylex_chess_clock.chessclock.util

import java.util.concurrent.TimeUnit

class TopLevelFiles {
    companion object {
        val clockTimes = listOf(
                TimeMapper(30, TimeUnit.SECONDS),
                TimeMapper(45, TimeUnit.SECONDS),
                TimeMapper(1, TimeUnit.MINUTES),
                TimeMapper(2, TimeUnit.MINUTES),
                TimeMapper(3, TimeUnit.MINUTES),
                TimeMapper(4, TimeUnit.MINUTES),
                TimeMapper(5, TimeUnit.MINUTES),
                TimeMapper(6, TimeUnit.MINUTES),
                TimeMapper(7, TimeUnit.MINUTES),
                TimeMapper(8, TimeUnit.MINUTES),
                TimeMapper(10, TimeUnit.MINUTES),
                TimeMapper(15, TimeUnit.MINUTES),
                TimeMapper(20, TimeUnit.MINUTES),
                TimeMapper(25, TimeUnit.MINUTES),
                TimeMapper(30, TimeUnit.MINUTES),
                TimeMapper(45, TimeUnit.MINUTES),
                TimeMapper(60, TimeUnit.MINUTES),
                TimeMapper(90, TimeUnit.MINUTES),
                TimeMapper(120, TimeUnit.MINUTES),
                TimeMapper(150, TimeUnit.MINUTES),
                TimeMapper(180, TimeUnit.MINUTES)
        )

        val incrementTimes = listOf(
                TimeMapper(0, TimeUnit.SECONDS),
                TimeMapper(1, TimeUnit.SECONDS),
                TimeMapper(2, TimeUnit.SECONDS),
                TimeMapper(3, TimeUnit.SECONDS),
                TimeMapper(4, TimeUnit.SECONDS),
                TimeMapper(5, TimeUnit.SECONDS),
                TimeMapper(7, TimeUnit.SECONDS),
                TimeMapper(8, TimeUnit.SECONDS),
                TimeMapper(9, TimeUnit.SECONDS),
                TimeMapper(10, TimeUnit.SECONDS),
                TimeMapper(15, TimeUnit.SECONDS),
                TimeMapper(20, TimeUnit.SECONDS),
                TimeMapper(25, TimeUnit.SECONDS),
                TimeMapper(30, TimeUnit.SECONDS),
                TimeMapper(45, TimeUnit.SECONDS),
                TimeMapper(60, TimeUnit.SECONDS),
                TimeMapper(90, TimeUnit.SECONDS),
                TimeMapper(120, TimeUnit.SECONDS),
                TimeMapper(150, TimeUnit.SECONDS),
                TimeMapper(180, TimeUnit.SECONDS)
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