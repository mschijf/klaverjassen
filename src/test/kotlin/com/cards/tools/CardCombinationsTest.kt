package com.cards.tools

import com.cards.game.card.Card
import org.junit.jupiter.api.Test

class CardCombinationsTest {
    @Test
    fun getPossibleCardCombinationsTest() {
        val combiClass = CardCombinations()

        val abc = listOf(2,2,1)
        val (a,b,c) = abc

        val yy = combiClass.getPossibleCardCombinations(
            a, b, c,
            Card.ofList("7H 8H 10H AH").toSet(),
            Card.ofList("7H 8H 10H AH").toSet(),
            Card.ofList("7H 8H 10H AH").toSet(),
            Card.ofList("JC").toSet(),
            emptySet(),
            emptySet())

        yy.forEach { combi -> println(combi) }

    }

    @Test
    fun getPossibleCombinationsTest() {
        val combiClass = CardCombinations()

        val abc = listOf(2,2,1)
        val (a,b,c) = abc

        val yy = combiClass.getPossibleCombinations(a, b, c,
            setOf(0, 1,4), setOf(0, 1,2,3,4), setOf(0, 1,2,3,4),
            setOf(),emptySet(),emptySet())

        yy.forEach { combi -> println(combi) }
    }

    @Test
    fun combinationPossibleCardsInTriplesTest() {
        val combiClass = CardCombinations()

        val abc = listOf(2,2,1)
        val (a,b,c) = abc
        val xx = combiClass.combinationsChatGPT(a, b, c)

        xx.forEach { combi ->
            println(combi)
        }
    }

}