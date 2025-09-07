package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import org.junit.jupiter.api.Test

class BruteForceTest {

    @Test
    fun mostValuableCardToPlayDifferentChoiceOmdatWeNatGaan() {
        val player = prepareGameAndPlayer2()
        val analyzer = KlaverjassenAnalyzer(player)
        val analysis = analyzer.refreshAnalysis()

        val bf = BruteForceRule(player, analysis)

        println( bf.chooseCard())
    }

    @Test
    fun mostValuableCardToPlay() {
        val player = prepareGameAndPlayer1()
        val analyzer = KlaverjassenAnalyzer(player)
        val analysis = analyzer.refreshAnalysis()

        val bf = BruteForceRule(player, analysis)

        println( bf.chooseCard())

    }

    //------------------------------------------------------------------------------------------------------------------

    private fun prepareGameAndPlayer1(): GeniusPlayerKlaverjassen {
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
        playerSouth.setCardsInHand(Card.ofList("KH AH"))

        return playerSouth
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun prepareGameAndPlayer2(): GeniusPlayerKlaverjassen {
        val game = Game()
        game.startNewRound(CardColor.HEARTS, TableSide.WEST)
        val cardsPlayed = Card.ofList("JH, 8H, 10H, 9C, KH, 9H, AH, JC, KD, 7D, AD, QD, QC, 8C, AC, 7H, 7S, KS, 8S, 9S, KC, 10C, 7C, JS")
        cardsPlayed.forEach { card -> game.playCard(card)}

        val playerWest = GeniusPlayerKlaverjassen(TableSide.WEST, game)
        playerWest.setCardsInHand(Card.ofList("QH JD"))

        return playerWest
    }

}