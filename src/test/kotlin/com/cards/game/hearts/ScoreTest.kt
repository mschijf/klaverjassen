package com.cards.game.hearts

import com.cards.game.fourplayercardgame.basic.TableSide
import org.junit.jupiter.api.Test

internal class ScoreTest {

    @Test
    fun plusTest() {
        val x = TableSide.values().associateWith { p -> 0 }

        println(x)
    }
}