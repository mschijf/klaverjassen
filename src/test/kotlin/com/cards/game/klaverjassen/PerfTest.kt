package com.cards.game.klaverjassen

import com.cards.game.card.CARDDECK
import com.cards.game.card.CardColor
import com.cards.tools.RANDOMIZER
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PerfTest {

    @Test
    fun perftMain() {
        RANDOMIZER.setFixedSequence(true)
        val cardSet = CARDDECK.baseDeckCardsSevenAndHigher

        val game = Game()
        game.startNewRound(CardColor.CLUBS, TableSide.WEST)

        println("  :  r    t")
        cardSet.forEach{card ->
            game.playCard(card)
            println("$card:  ${game.getRounds().size}   ${game.getCurrentRound().getTrickList().size}")
        }
        println("----------------------------------------------")

        repeat(32) {
            val card = game.takeLastCardBack()
            println("$card:  ${game.getRounds().size}   ${game.getCurrentRound().getTrickList().size}")
        }
    }

}