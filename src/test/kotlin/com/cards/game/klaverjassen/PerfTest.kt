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

        cardSet.forEach{card ->
            game.playCard(card)
        }
        val trickLead = game.getCurrentRound().getTrickList().last().getSideToLead()
        var card = cardSet.first()
        repeat(5) {
            card = game.takeLastCardBack()
        }
        game.playCard(card)
        assertEquals(CardColor.CLUBS, game.getCurrentRound().getTrumpColor())
        assertEquals(0, game.getCurrentRound().getTrickOnTable().getCardsPlayed().size)
        assertEquals(1, game.getRounds().size)
        assertEquals(8, game.getCurrentRound().getTrickList().size)
        assertEquals(trickLead, game.getCurrentRound().getTrickOnTable().getSideToLead())

        repeat(28) {
            card = game.takeLastCardBack()
        }

        assertEquals(CardColor.CLUBS, game.getCurrentRound().getTrumpColor())
        assertEquals(0, game.getCurrentRound().getTrickOnTable().getCardsPlayed().size)
        assertEquals(1, game.getRounds().size)
        assertEquals(1, game.getCurrentRound().getTrickList().size)
    }

}