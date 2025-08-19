package com.cards.tools

import kotlin.random.Random

object RANDOMIZER {
    private var fixedSeed : Int? = null
    private var lastSeedUsed = 0
    private var fixedSequence = false

    fun setSeed(seed: Int) {
        fixedSeed = seed
    }
    fun unsetSeed() {
        fixedSeed = null
    }
    fun setFixedSequence(fixed: Boolean) {
        fixedSequence = fixed
    }

    fun getShuffleRandomizer(): Random {
        lastSeedUsed =  if (fixedSeed != null) {
            fixedSeed!!
        } else if (fixedSequence) {
            Random(lastSeedUsed).nextInt(0, Int.MAX_VALUE)
        } else {
            Random.nextInt(0, Int.MAX_VALUE)
        }
        return Random(lastSeedUsed)
    }

    fun getLastSeedUsed() = lastSeedUsed
}