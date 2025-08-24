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
    fun mostValuableCardToPlayDifferentChoiceOmdatWeNatGaan() {
        val analyzer = setUpAnalyzer2()
        val player = prepareGameAndPlayer2(analyzer)

        val bf = BruteForce(player, analyzer)

        println( bf.mostValuableCardToPlay())
    }

    @Test
    fun mostValuableCardToPlay() {
        val analyzer = setUpAnalyzer1()
        val player = prepareGameAndPlayer1(analyzer)

        val bf = BruteForce(player, analyzer)

        println( bf.mostValuableCardToPlay())

    }

    //------------------------------------------------------------------------------------------------------------------

    fun setUpAnalyzer1():KlaverjassenAnalyzer {
        val analyzer: KlaverjassenAnalyzer = mockk()
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

        return analyzer
    }

    private fun prepareGameAndPlayer1(analyzer: KlaverjassenAnalyzer): GeniusPlayerKlaverjassen {
        val game = Game()
        game.startNewRound(CardColor.CLUBS, TableSide.WEST)

        val clubs = Card.ofList("7C 8C 9C 10C JC QC KC AC")
        val diamonds = Card.ofList("7D 8D 9D 10D JD QD KD AD")
        val spades = Card.ofList("7S 8S 9S 10S JS QS KS AS")
        val hearts = Card.ofList("7H")

        clubs.forEach { card -> game.playCard(card)}
        diamonds.forEach { card -> game.playCard(card)}
        spades.forEach { card -> game.playCard(card)}
        hearts.forEach { card -> game.playCard(card)}

        val playerSouth = GeniusPlayerKlaverjassen(TableSide.SOUTH, game)
        playerSouth.setCardsInHand(analyzer.playerSureHasCards(TableSide.SOUTH).toList())

        return playerSouth
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun prepareGameAndPlayer2(analyzer: KlaverjassenAnalyzer): GeniusPlayerKlaverjassen {
        val game = Game()
        game.startNewRound(CardColor.HEARTS, TableSide.WEST)
        val cardsPlayed = Card.ofList("JH, 8H, 10H, 9C, KH, 9H, AH, JC, KD, 7D, AD, QD, QC, 8C, AC, 7H, 7S, KS, 8S, 9S, KC, 10C, 7C, JS")
        cardsPlayed.forEach { card -> game.playCard(card)}

        val playerWest = GeniusPlayerKlaverjassen(TableSide.WEST, game)
        playerWest.setCardsInHand(analyzer.playerSureHasCards(TableSide.WEST).toList())

        return playerWest
    }

    fun setUpAnalyzer2():KlaverjassenAnalyzer {
        val analyzer: KlaverjassenAnalyzer = mockk()

        every { analyzer.playerSureHasCards(TableSide.SOUTH) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.NORTH) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.EAST) } returns emptySet()

        every { analyzer.playerCanHaveCards(TableSide.SOUTH) } returns
                Card.ofList("8D 9D 10D 10S QS AS").toSet()
        every { analyzer.playerCanHaveCards(TableSide.NORTH) } returns
                Card.ofList("8D 9D 10D 10S QS AS").toSet()
        every { analyzer.playerCanHaveCards(TableSide.EAST) } returns
                Card.ofList("8D 9D 10D 10S QS AS").toSet()

        every { analyzer.playerCanHaveCards(TableSide.WEST) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.WEST) } returns
                Card.ofList("QH JD").toSet()

        every { analyzer.cardsInHandForSide(TableSide.WEST)} returns 2
        every { analyzer.cardsInHandForSide(TableSide.NORTH)} returns 2
        every { analyzer.cardsInHandForSide(TableSide.EAST)} returns 2
        every { analyzer.cardsInHandForSide(TableSide.SOUTH)} returns 2

        return analyzer
    }

}