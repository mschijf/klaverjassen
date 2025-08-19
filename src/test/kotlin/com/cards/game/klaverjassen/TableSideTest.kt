package com.cards.game.klaverjassen

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TableSideTest {
    @Test
    fun clockwiseDistanceFrom() {
        assertEquals(3, TableSide.WEST.clockwiseDistanceFrom(TableSide.SOUTH))
    }

    @Test
    fun clockwiseDistanceTo() {
        assertEquals(1, TableSide.WEST.clockwiseDistanceTo(TableSide.SOUTH))
    }

    @Test
    fun clockwiseNext() {
        assertEquals(TableSide.WEST, TableSide.WEST.clockwiseNext(0))
        assertEquals(TableSide.NORTH, TableSide.WEST.clockwiseNext())
        assertEquals(TableSide.EAST, TableSide.WEST.clockwiseNext(2))
        assertEquals(TableSide.SOUTH, TableSide.WEST.clockwiseNext(3))
        assertEquals(TableSide.EAST, TableSide.WEST.clockwiseNext(6))
    }
}