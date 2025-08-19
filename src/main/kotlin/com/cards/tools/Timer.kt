package com.cards.tools

import java.time.Duration
import java.time.Instant

class Timer(
    private val printAll: Boolean = false) {
    private val timerMap = mutableMapOf<String, Instant>()
    private var level = 0

    fun startTimer(timerName: String) {
        timerMap.put(timerName, Instant.now())
        ++level
    }

    fun stopTimer(timerName: String, extraInfo: Long?=null, forcePrint : Boolean=false):Long {
        --level
        val timePassed = checkTimer(timerName)
        if (printAll || forcePrint) {
            print("%-40s: %,5d ms".format(leadingSpaces(level) + timerName, timePassed))
            if (extraInfo == null) {
                println()
            } else {
                println(" --> %,15d".format(extraInfo))
            }
        }
        timerMap.remove(timerName)
        return timePassed
    }

    fun checkTimer(timerName: String):Long {
        return if (hasTimerFor(timerName))
            Duration.between(timerMap[timerName], Instant.now()).toMillis()
        else
            0L
    }

    fun hasTimerFor(name: String) : Boolean = timerMap.containsKey(name)

    private fun leadingSpaces(level: Int) : String {
        return "".padStart(level*2)
    }



}

