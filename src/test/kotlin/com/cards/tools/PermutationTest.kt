package com.cards.tools

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PermutationTest {
    @Test
    fun combinations() {
        assertEquals(15, combinations(6, 2))
        assertEquals(-1, combinations(3, 4))
        assertEquals(1, combinations(4, 4))
        assertEquals(1, combinations(4, 0))
        assertEquals(4, combinations(4, 1))
    }

    @Test
    fun combinationsBig() {
        assertEquals(45, combinations(10, 2))
        assertEquals(735471, combinations(24, 8))
        assertEquals(2704156, combinations(24, 12))
        assertEquals(1, combinations(24, 24))
        assertEquals(1, combinations(24, 0))
        assertEquals(24, combinations(24, 1))
    }

    @Test
    fun combinationsCards() {
        assertEquals(45, cardCombinations(10, 2))
        assertEquals(735471, cardCombinations(24, 8))
        assertEquals(2704156, cardCombinations(24, 12))
        assertEquals(1, cardCombinations(24, 24))
        assertEquals(1, cardCombinations(24, 0))
        assertEquals(24, cardCombinations(24, 1))
    }
}