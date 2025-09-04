package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.klaverjassen.GameStatus
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.tools.CardCombinations
import kotlin.math.max
import kotlin.math.min

class BruteForce(
    val playerForWhichWeAnalyze: GeniusPlayerKlaverjassen,
    val brainDump: BrainDump) {

    private val game = playerForWhichWeAnalyze.game
    private val fakeGroup = createFakeGroup()
    private val combiClass = CardCombinations()

    fun mostValuableCardToPlay(): Card {
        val combinations = getCombinations()
        val cardValueList = combinations.map { combination ->
            fakeGroup.getPlayer(brainDump.p1).setCardsInHand(combination.first)
            fakeGroup.getPlayer(brainDump.p2).setCardsInHand(combination.second)
            fakeGroup.getPlayer(brainDump.p3).setCardsInHand(combination.third)
            val valuePerCard = tryCard()
//            println("$combination    -->  %-3s %4d %-3s %4d ".format(valuePerCard[0].card, valuePerCard[0].value, valuePerCard[1].card, valuePerCard[1].value))
            valuePerCard
        }
        val totalCardValue = playerForWhichWeAnalyze.getCardsInHand().map{card -> card to cardValueList.flatten().filter { it.card == card }.sumOf { it.value }}
        return totalCardValue.maxBy { it.second }.first

        // todo: find out best analytical card playing choice
        //keuze: welke kaart is meeste keren winnaar?
        //       welke kaart levert als som de meeste punten op ('=average')
        //       wat is de modus ('=modus')

    }

    private fun tryCard(): List<CardValue> {
        val result = mutableListOf<CardValue>()
        playerForWhichWeAnalyze.getCardsInHand().forEach { card ->
            playCard(playerForWhichWeAnalyze, card)
            val value = tryRestOfGame(Int.MIN_VALUE, Int.MAX_VALUE)
            takeCardBack(playerForWhichWeAnalyze, card)
            result.add(CardValue(card, value))
        }
        return result
    }

    private fun tryRestOfGame(alfa: Int, beta: Int): Int {
        val sideToMove = game.getSideToMove()
        if (game.getCurrentRound().isComplete()) {
            return game.getCurrentRound().getScore().getDeltaForPlayer(tableSide = playerForWhichWeAnalyze.tableSide)
        }

        val playerToMove = fakeGroup.getPlayer(sideToMove)
        if (sideToMove == playerForWhichWeAnalyze.tableSide || sideToMove == brainDump.partner) {
            var best = Int.MIN_VALUE
            playerToMove.getLegalPlayableCards().forEach { card ->
                playCard(playerToMove, card)
                val v = tryRestOfGame(max(alfa, best), beta)
                takeCardBack(playerToMove, card)
                if (v > best)
                    best = v
                if (v >= beta)
                    return v
            }
            return best

        } else {
            var best = Int.MAX_VALUE
            playerToMove.getLegalPlayableCards().forEach { card ->
                playCard(playerToMove, card)
                val v = tryRestOfGame(alfa, min(beta, best))
                takeCardBack(playerToMove, card)
                if (v < best)
                    best = v
                if (v <= alfa)
                    return v
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
                Player(brainDump.p1, game),
                Player(brainDump.p2, game),
                Player(brainDump.p3, game))
            )
    }

    private fun getCombinations(): List<Triple<List<Card>, List<Card>, List<Card>>> {
        return combiClass.getPossibleCardCombinations(
            brainDump.player1.numberOfCardsInHand,
            brainDump.player2.numberOfCardsInHand,
            brainDump.player3.numberOfCardsInHand,

            brainDump.player1.canHave,
            brainDump.player2.canHave,
            brainDump.player3.canHave,

            brainDump.player1.sureHas,
            brainDump.player2.sureHas,
            brainDump.player3.sureHas,)
    }
}