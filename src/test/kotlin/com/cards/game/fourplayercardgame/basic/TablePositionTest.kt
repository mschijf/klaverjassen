package com.cards.game.fourplayercardgame.basic

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TablePositionTest {
    @Test
    fun clockwiseNext() {
        assertEquals(TableSide.WEST, TableSide.WEST.clockwiseNext(0))
        assertEquals(TableSide.NORTH, TableSide.WEST.clockwiseNext())
        assertEquals(TableSide.EAST, TableSide.WEST.clockwiseNext(2))
        assertEquals(TableSide.SOUTH, TableSide.WEST.clockwiseNext(3))
        assertEquals(TableSide.EAST, TableSide.WEST.clockwiseNext(6))
    }

}