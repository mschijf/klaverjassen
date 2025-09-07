package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.klaverjassen.GameStatus
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.tools.CardCombinations
import kotlin.math.max
import kotlin.math.min

class BruteForceRule(player: Player, brainDump: BrainDump): AbstractChooseCardRule(player, brainDump) {

    private val fakeGroup = createFakeGroup()
    private val combiClass = CardCombinations()

    override fun chooseCard(): Card {
        val combinations = getCombinations()
        val cardValueList = combinations.map { combination ->
            fakeGroup.getPlayer(brainDump.p1).setCardsInHand(combination.first)
            fakeGroup.getPlayer(brainDump.p2).setCardsInHand(combination.second)
            fakeGroup.getPlayer(brainDump.p3).setCardsInHand(combination.third)
            val valuePerCard = tryCard()
//            println("$combination    -->  %-3s %4d %-3s %4d ".format(valuePerCard[0].card, valuePerCard[0].value, valuePerCard[1].card, valuePerCard[1].value))
            valuePerCard
        }
        val totalCardValue = myLegalCards.map{card -> card to cardValueList.flatten().filter { it.card == card }.sumOf { it.value }}
        return totalCardValue.maxBy { it.second }.first

        // todo: find out best analytical card playing choice
        //keuze: welke kaart is meeste keren winnaar?
        //       welke kaart levert als som de meeste punten op ('=average')
        //       wat is de modus ('=modus')
    }

    private fun tryCard(): List<CardValue> {
        val result = mutableListOf<CardValue>()
        myLegalCards.forEach { card ->
            playCard(player, card)
            val value = tryRestOfGame(Int.MIN_VALUE, Int.MAX_VALUE)
            takeCardBack(player, card)
            result.add(CardValue(card, value))
        }
        return result
    }

    private fun tryRestOfGame(alfa: Int, beta: Int): Int {
        val sideToMove = currentGame.getSideToMove()
        if (currentRound.isComplete()) {
            return currentRound.getScore().getDeltaForPlayer(tableSide = mySide)
        }

        val playerToMove = fakeGroup.getPlayer(sideToMove)
        if (sideToMove == mySide || sideToMove == brainDump.partner) {
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
        return currentGame.playCard(card)
    }

    private fun takeCardBack(player: Player, card: Card) {
        currentGame.takeLastCardBack()
        player.addCard(card)
    }

    private fun createFakeGroup(): PlayerGroup {

        return PlayerGroup(
            listOf(
                player,
                Player(brainDump.p1, currentGame),
                Player(brainDump.p2, currentGame),
                Player(brainDump.p3, currentGame))
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