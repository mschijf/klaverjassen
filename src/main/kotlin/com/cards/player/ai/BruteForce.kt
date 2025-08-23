package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.klaverjassen.GameStatus
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.tools.CardCombinations

class BruteForce(
    val playerForWhichWeAnalyze: GeniusPlayerKlaverjassen,
    val analyzer: KlaverjassenAnalyzer) {

    private val player1 = playerForWhichWeAnalyze.tableSide.clockwiseNext()
    private val player2 = player1.clockwiseNext()
    private val player3 = player2.clockwiseNext()

    private val game = playerForWhichWeAnalyze.game
    private val fakeGroup = createFakeGroup()
    private val combiClass = CardCombinations()


    fun mostValuableCardToPlay(): Card {
        val combinations = getCombinations()
        val cardValueList = combinations.map { combination ->
            fakeGroup.getPlayer(player1).setCardsInHand(combination.first)
            fakeGroup.getPlayer(player2).setCardsInHand(combination.second)
            fakeGroup.getPlayer(player3).setCardsInHand(combination.third)
            val valuePerCard = tryCard()
//            println("$combination    -->  %-3s %4d %-3s %4d ".format(valuePerCard[0].card, valuePerCard[0].value, valuePerCard[1].card, valuePerCard[1].value))
            valuePerCard
        }
        val totalCardValue = playerForWhichWeAnalyze.getCardsInHand().map{card -> card to cardValueList.flatten().filter { it.card == card }.sumOf { it.value }}
        return totalCardValue.maxBy { it.second }.first
    }

    private fun tryCard(): List<CardValue> {
        val result = mutableListOf<CardValue>()
        playerForWhichWeAnalyze.getCardsInHand().forEach { card ->
            playCard(playerForWhichWeAnalyze, card)
            val value = tryRestOfGame()
            takeCardBack(playerForWhichWeAnalyze, card)
            result.add(CardValue(card, value))
        }
        return result
    }

    private fun tryRestOfGame(): Int {
        val sideToMove = game.getSideToMove()
        if (game.getCurrentRound().isComplete()) {
            return game.getCurrentRound().getScore().getDeltaForPlayer(tableSide = playerForWhichWeAnalyze.tableSide)
        }

        val playerToMove = fakeGroup.getPlayer(sideToMove)
        if (sideToMove == playerForWhichWeAnalyze.tableSide || sideToMove == playerForWhichWeAnalyze.tableSide.opposite()) {
            var best = Int.MIN_VALUE
            playerToMove.getLegalPlayableCards().forEach { card ->
                playCard(playerToMove, card)
                val v = tryRestOfGame()
                takeCardBack(playerToMove, card)
                if (v > best)
                    best = v
            }
            return best

        } else {
            var best = Int.MAX_VALUE
            playerToMove.getLegalPlayableCards().forEach { card ->
                playCard(playerToMove, card)
                val v = tryRestOfGame()
                takeCardBack(playerToMove, card)
                if (v < best)
                    best = v
            }
            return best
        }
    }

    private fun playCard(player: Player, card: Card): GameStatus {
        player.removeCard(card)
        return game.playCard(card)
    }

    private fun takeCardBack(player: Player, card: Card) {
        game.takeLastCardBack()
        player.addCard(card)
    }

    private fun createFakeGroup(): PlayerGroup {

        return PlayerGroup(
            listOf(
                playerForWhichWeAnalyze,
                Player(player1, game),
                Player(player2, game),
                Player(player3, game))
            )
    }

    private fun getCombinations(): List<Triple<List<Card>, List<Card>, List<Card>>> {
        val yy = combiClass.getPossibleCardCombinations(
            analyzer.cardsInHandForSide(player1),
            analyzer.cardsInHandForSide(player2),
            analyzer.cardsInHandForSide(player3),

            analyzer.playerCanHaveCards(player1),
            analyzer.playerCanHaveCards(player2),
            analyzer.playerCanHaveCards(player3),

            analyzer.playerSureHasCards(player1),
            analyzer.playerSureHasCards(player2),
            analyzer.playerSureHasCards(player3))

        return yy
    }
}

data class CardValue(val card: Card, val value: Int)