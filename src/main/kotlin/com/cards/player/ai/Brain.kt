package com.cards.player.ai

import com.cards.game.card.CARDDECK
import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.*
import com.cards.player.Player
import kotlin.contracts.contract

data class Brain(
    val myCardsInHand: List<Card>,
    val myLegalCards: List<Card>,
    val allCardsInPlay: List<Card>,
    val p1: TableSide,
    val p2: TableSide,
    val p3: TableSide,
    val partner: TableSide,
    val contractOwner: TableSide,
    val trump: CardColor,
    val leadColor: CardColor?,
    val iAmFirstPlayer: Boolean,
    val iAmSecondPlayer: Boolean,
    val iAmThirdPlayer: Boolean,
    val iAmFourthPlayer: Boolean,
    val legalCardsByColor: Map<CardColor, List<Card>>,
    val player1: OtherPlayer,
    val player2: OtherPlayer,
    val player3: OtherPlayer, ) {

    private val playerMap = mapOf(p1 to player1, p2 to player2, p3 to player3,)
    fun player(side: TableSide) = playerMap[side]?:throw Exception("asked for wrong player")

    val partnerCards = player(partner).allAssumeCards
    val partnerCardColors = partnerCards.map {it.color}.toSet()

    fun cardsInPlayOtherPlayers() = allCardsInPlay - myCardsInHand

    fun hasColor(cardColor: CardColor) = myCardsInHand.any{it.color == cardColor}
    fun canFollow() = leadColor?.let{ color -> hasColor(color) } ?: false
    fun hasTroef() = hasColor(trump)
    fun mustTroeven() = !canFollow() && hasTroef()

    val theyOwnContract = contractOwner == p1 || contractOwner == p3
    val iAmContractOwnersPartner = contractOwner == p2
    val iAmContractOwner = !theyOwnContract && !iAmContractOwnersPartner

    //------------------------------------------------------------------------------------------------------------------


    fun printAnalyzer() {
        TableSide.values().forEach {
            val playerCanHaveCards = player(it).canHave
            print(String.format("%-5s ", it.toString().lowercase()))
            print(String.format("(%2d): ", playerCanHaveCards.size))
            CardColor.values().forEach { color ->
                print(String.format("%-8s: %-25s  ", color, playerCanHaveCards.filter{card->card.color == color}.map { card -> card.rank.rankString }))
            }
            println()
            val playerSureHasCards = player(it).sureHas
            print(String.format("%-5s ", it.toString().lowercase()))
            print(String.format("(%2d): ", playerSureHasCards.size))
            CardColor.values().forEach { color ->
                print(String.format("%-8s: %-25s  ", " ", playerSureHasCards.filter{card->card.color == color}.map { card -> card.rank.rankString }))
            }
            println()
        }
    }

    companion object {

        fun reset(player: Player,
                  playerCanHave: Map<TableSide, Set<Card>>,
                  playerSureHas: Map<TableSide, Set<Card>>,
                  playerProbablyHas: Map<TableSide, Set<Card>>,
                  playerProbablyHasNot: Map<TableSide, Set<Card>>,): Brain {

            val currentTrick = player.game.getCurrentRound().getTrickOnTable()
            val nTrickCardsPlayed = currentTrick.getCardsPlayed().size
            val trump = player.game.getCurrentRound().getTrumpColor()

            val myCards = player.getCardsInHand()
            val nCardsInHand = myCards.size
            val sidesPlayedInTrick = currentTrick.getSidesPlayed()
            val legalCards = player.getLegalPlayableCards()
            val p1 = player.tableSide.clockwiseNext(1)
            val p2 = player.tableSide.clockwiseNext(2)
            val p3 = player.tableSide.clockwiseNext(3)

            fun numberOfCardsInHandForSide(side: TableSide) = nCardsInHand - if (side in sidesPlayedInTrick) 1 else 0

            fun otherPlayerCanHaveLegalCards(playerSide: TableSide): Set<Card> {
                return (playerCanHave[playerSide]!! + playerSureHas[playerSide]!!)
                    .legalPlayable(currentTrick, trump)
                    .toSet()
            }


            return Brain (
                myCardsInHand = myCards,
                myLegalCards = legalCards,
                allCardsInPlay = CARDDECK.baseDeckCardsSevenAndHigher - player.game.getCurrentRound().getTrickList().flatMap { it.getCardsPlayed() },
                p1 = p1,
                p2 = p2,
                p3 = p3,
                partner = p2,
                contractOwner = player.game.getCurrentRound().getContractOwningSide(),
                trump = player.game.getCurrentRound().getTrumpColor(),
                leadColor = currentTrick.getLeadColor(),
                iAmFirstPlayer = nTrickCardsPlayed == 0,
                iAmSecondPlayer = nTrickCardsPlayed == 1,
                iAmThirdPlayer = nTrickCardsPlayed == 2,
                iAmFourthPlayer = nTrickCardsPlayed == 3,
                legalCardsByColor = legalCards.groupBy { it.color },
                player1 = OtherPlayer(numberOfCardsInHandForSide(p1), playerCanHave[p1]!!, playerSureHas[p1]!!, otherPlayerCanHaveLegalCards(p1)),
                player2 = OtherPlayer(numberOfCardsInHandForSide(p2), playerCanHave[p2]!!, playerSureHas[p2]!!, otherPlayerCanHaveLegalCards(p2)),
                player3 = OtherPlayer(numberOfCardsInHandForSide(p3), playerCanHave[p3]!!, playerSureHas[p3]!!, otherPlayerCanHaveLegalCards(p3))
            )
        }

    }


}

data class OtherPlayer(
    val numberOfCardsInHand: Int,
    val canHave: Set<Card>,
    val sureHas: Set<Card>,
    val legalCards: Set<Card>,) {
    val allAssumeCards = canHave + sureHas
}