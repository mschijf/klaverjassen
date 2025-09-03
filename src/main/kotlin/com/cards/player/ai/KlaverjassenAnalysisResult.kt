package com.cards.player.ai

import com.cards.game.card.CARDDECK
import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.Round
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.Trick
import com.cards.game.klaverjassen.legalPlayable

data class KlaverjassenAnalysisResult(
    private val game: Game,
    private val currentRound: Round,
    private val currentTrick: Trick,
    val mySide: TableSide,
    val cardsInHand: List<Card>,
    val legalCards: List<Card>,
    val playerCanHave: Map<TableSide, Set<Card>>,
    val playerSureHas: Map<TableSide, Set<Card>>,
    val playerProbablyHas: Map<TableSide, Set<Card>>,
    val playerProbablyHasNot: Map<TableSide, Set<Card>>) {

    val allCardsInPlay = CARDDECK.baseDeckCardsSevenAndHigher - currentRound.getTrickList().flatMap { it.getCardsPlayed() }

    val player1 = mySide.clockwiseNext(1)
    val player2 = mySide.clockwiseNext(2)
    val player3 = mySide.clockwiseNext(3)
    val partner = player2

    val trump = currentRound.getTrumpColor()
    val leadColor = currentTrick.getLeadColor()

    val iAmFirstPlayer = currentTrick.getCardsPlayed().size == 0
    val iAmSecondPlayer = currentTrick.getCardsPlayed().size == 1
    val iAmThirdPlayer = currentTrick.getCardsPlayed().size == 2
    val iAmFourthPlayer = currentTrick.getCardsPlayed().size == 3

    val legalCardsByColor = legalCards.groupBy { it.color }
    val partnerCards = playerAssumptionCards(partner)
    val partnerCardColors = partnerCards.map {it.color}.toSet()

    fun playerCanHaveCards(side: TableSide): Set<Card> = playerCanHave[side]!!
    fun playerSureHasCards(side: TableSide): Set<Card> = playerSureHas[side]!!
    fun playerAssumptionCards(side: TableSide) = playerCanHaveCards(side) + playerSureHasCards(side)
    fun cardsInPlayOtherPlayers() = allCardsInPlay - cardsInHand

    fun hasColor(cardColor: CardColor) = cardsInHand.any{it.color == cardColor}
    fun canFollow() = leadColor?.let{ color -> hasColor(color) } ?: false
    fun hasTroef() = hasColor(trump)
    fun mustTroeven() = !canFollow() && hasTroef()

    val theyOwnContract = currentRound.isContractOwningSide(player1) || game.getCurrentRound().isContractOwningSide(player3)
    val iAmContractOwner = currentRound.isContractOwningSide(mySide)
    val iAmContractOwnersPartner = currentRound.isContractOwningSide(partner)

    fun numberOfCardsInHandForSide(side: TableSide): Int {
        return if (side in currentTrick.getSidesPlayed())
            cardsInHand.size - 1
        else
            cardsInHand.size
    }

    fun otherPlayerCanHaveLegalCards(playerSide: TableSide): List<Card> {
        return (playerCanHaveCards(playerSide) + playerSureHasCards(playerSide))
            .toList()
            .legalPlayable(currentTrick, trump)
    }

    //------------------------------------------------------------------------------------------------------------------


    fun printAnalyzer() {
        TableSide.values().forEach {
            val playerCanHaveCards = playerCanHaveCards(it)
            print(String.format("%-5s ", it.toString().lowercase()))
            print(String.format("(%2d): ", playerCanHaveCards.size))
            CardColor.values().forEach { color ->
                print(String.format("%-8s: %-25s  ", color, playerCanHaveCards.filter{card->card.color == color}.map { card -> card.rank.rankString }))
            }
            println()
            val playerSureHasCards = playerSureHasCards(it)
            print(String.format("%-5s ", it.toString().lowercase()))
            print(String.format("(%2d): ", playerSureHasCards.size))
            CardColor.values().forEach { color ->
                print(String.format("%-8s: %-25s  ", " ", playerSureHasCards.filter{card->card.color == color}.map { card -> card.rank.rankString }))
            }
            println()
        }
    }
}
