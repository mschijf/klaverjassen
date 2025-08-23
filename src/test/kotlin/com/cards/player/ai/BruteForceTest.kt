package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class BruteForceTest {

    @Test
    fun mostValuableCardToPlay() {
        val game = Game()
        game.startNewRound(CardColor.CLUBS, TableSide.WEST)
        prepareGame(game)

        val analyzer: KlaverjassenAnalyzer = mockk()
        setUp(analyzer)

        val playerSouth = GeniusPlayerKlaverjassen(TableSide.SOUTH, game)
        playerSouth.setCardsInHand(Card.ofList("KH AH"))

        val bf = BruteForce(playerSouth, analyzer)

        println( bf.mostValuableCardToPlay())

    }

    fun setUp(analyzer: KlaverjassenAnalyzer) {
        every { analyzer.playerSureHasCards(TableSide.WEST) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.NORTH) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.EAST) } returns emptySet()

        every { analyzer.playerCanHaveCards(TableSide.WEST) } returns
                Card.ofList("8H 9H 10H JH QH").toSet()
        every { analyzer.playerCanHaveCards(TableSide.NORTH) } returns
                Card.ofList("8H 9H 10H JH QH").toSet()
        every { analyzer.playerCanHaveCards(TableSide.EAST) } returns
                Card.ofList("8H 9H 10H JH QH").toSet()

        every { analyzer.playerCanHaveCards(TableSide.SOUTH) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.SOUTH) } returns
                Card.ofList("KH AH").toSet()

        every { analyzer.cardsInHandForSide(TableSide.WEST)} returns 2
        every { analyzer.cardsInHandForSide(TableSide.NORTH)} returns 2
        every { analyzer.cardsInHandForSide(TableSide.EAST)} returns 1
        every { analyzer.cardsInHandForSide(TableSide.SOUTH)} returns 2
    }

    private fun prepareGame(game: Game) {
        val clubs = Card.ofList("7C 8C 9C 10C JC QC KC AC")
        val diamonds = Card.ofList("7D 8D 9D 10D JD QD KD AD")
        val spades = Card.ofList("7S 8S 9S 10S JS QS KS AS")
        val hearts = Card.ofList("7H")

        clubs.forEach { card -> game.playCard(card)}
        diamonds.forEach { card -> game.playCard(card)}
        spades.forEach { card -> game.playCard(card)}
        hearts.forEach { card -> game.playCard(card)}
    }
}