package com.cards.tools

object Log {
    private val text: StringBuilder = StringBuilder()

    fun print(msg: String) {
        text.append(msg)
    }

    fun println(msg: String) {
        text.append(msg + "\n")
    }

    fun get() = text.toString()
}