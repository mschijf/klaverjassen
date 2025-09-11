package com.cards.player.ai.v0

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.*
import com.cards.player.Player
import com.cards.tools.Log
import tool.mylambdas.collectioncombination.mapCombinedItems
import kotlin.collections.filter
import kotlin.math.sign


const val ROEM_POSSIBLE_NEXT_TRICK_VALUE_MIN = 10
const val ROEM_POSSIBLE_NEXT_TRICK_VALUE_MAX = -10

abstract class AbstractChooseCardRule(protected val player: Player) {

    abstract fun chooseCard(): Card

    protected val memory = Memory.refresh(player)

    protected val currentGame = player.game
    protected val currentRound = currentGame.getCurrentRound()
    protected val currentTrick = currentRound.getTrickOnTable()
    protected val trump = player.game.getCurrentRound().getTrumpColor()
    protected val contractOwner = player.game.getCurrentRound().getContractOwningSide()
    protected val leadPlayer = currentTrick.getSideToLead()

    protected fun Card.cardValue() = this.cardValue(trump)
    protected fun iHaveCard(card: Card) = card in player.getCardsInHand()

    protected val trumpJack = Card(trump, CardRank.JACK)
    protected val trumpNine = Card(trump, CardRank.NINE)
    protected val trumpAce = Card(trump, CardRank.ACE)
    protected val trumpTen = Card(trump, CardRank.ACE)

    protected val mySide = player.tableSide
    protected val myCardsInHand = player.getCardsInHand()
    protected val myLegalCards = player.getLegalPlayableCards()
    protected val myLegalCardsByColor = myLegalCards.groupBy { it.color }

    protected val player1Side = player.tableSide.clockwiseNext(1)
    protected val player2Side = player.tableSide.clockwiseNext(2)
    protected val player3Side = player.tableSide.clockwiseNext(3)
    protected val partner = player2Side

    protected val nTrickCardsPlayed = currentTrick.getCardsPlayed().size
    protected val iAmFirstPlayer = nTrickCardsPlayed == 0
    protected val iAmSecondPlayer = nTrickCardsPlayed == 1
    protected val iAmThirdPlayer = nTrickCardsPlayed == 2
    protected val iAmFourthPlayer = nTrickCardsPlayed == 3

    protected val theyOwnContract = contractOwner == player1Side || contractOwner == player3Side
    protected val iAmContractOwnersPartner = contractOwner == player2Side
    protected val iAmContractOwner = !theyOwnContract && !iAmContractOwnersPartner

    private val playerMap = memory.playerList.associateBy { it.tableSide }
    protected fun player(side: TableSide) = playerMap[side]?:throw Exception("asked for wrong player")
    protected val player1 = playerMap[player1Side]!!
    protected val player2 = playerMap[player2Side]!!
    protected val player3 = playerMap[player3Side]!!

    protected val partnerCards = player(player2Side).allAssumeCards
    protected val partnerCardColors = partnerCards.map {it.color}.toSet()

    private val colorPlayedMap = memory.cardsPlayed.groupingBy { it.color }.eachCount()
    protected fun CardColor.colorPlayedCount() = colorPlayedMap[this]?:0

    protected fun CardColor.playedForFirstTime() = currentTrick.isLeadColor(this) &&
            player.game.getCurrentRound().getTrickList().dropLast(1).count{it.isLeadColor(this)} == 0
    protected fun CardColor.playedForSecondTime() = currentTrick.isLeadColor(this) &&
            player.game.getCurrentRound().getTrickList().dropLast(1).count{it.isLeadColor(this)} == 1
    protected fun CardColor.playedBefore() = !playedForFirstTime()

    protected fun Card.isTrump() = this.color == trump

    protected fun TableSide.isPartner() = this == partner
    protected fun TableSide.isOtherParty() = this == player1Side || this == player3Side
    protected fun TableSide.isContractOwner() = this == contractOwner

    protected fun Card.isKaal() = myLegalCardsByColor[this.color]!!.size == 1
    protected fun Card.isVrij() = memory.cardsInPlayOtherPlayers.none { it.color == this.color }

    //todo: check all highest algorithms
    protected fun Card.isHigherThanOtherInPlay() = memory.cardsInPlayOtherPlayers.none { it.color == this.color && it.beats(this, trump)}
    protected fun Card.isHigherThanAllInPlayIncludingMine() = memory.allCardsInPlay.none { it.color == this.color && it.beats(this, trump)}
    protected fun CardColor.highestInPlayOrOnTable() =
        (memory.cardsInPlayOtherPlayers + currentTrick.getCardsPlayed()).filter {it.color == this}.maxByOrNull { it.toRankNumber(trump) }

    protected fun CardColor.myHighest() = myLegalCardsByColor[this]?.maxBy { it.toRankNumber(trump) }
    protected fun CardColor.iHaveHighest() = myHighest()?.isHigherThanOtherInPlay()?:false

    protected val myTrumpCards = myCardsInHand.filter { it.isTrump()  }
    protected fun erIsEerderTroefGetrokken() = currentRound.getTrickList().dropLast(1).any { it.isLeadColor(trump)}
    protected fun othersCanHaveTroef() = memory.cardsInPlayOtherPlayers.any { it.isTrump() }
    protected fun opponentCanHaveTroef() = (player1.allAssumeCards + player3.allAssumeCards).any { it.isTrump() }
    protected fun partnerCanHaveTroef() = player2.allAssumeCards.any { it.isTrump() }


    //------------------------------------------------------------------------------------------------------------------

    protected fun playFallbackCard(info: String? = null): Card {
        if (info != null && doPrintFallBack) {
            Log.println("FALL BACK NOTE ($mySide): $info")
            println("FALL BACK NOTE ($mySide): $info")
        }
        return myLegalCards.minBy { it.color.ordinal * 100 + it.rank.ordinal }
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    //todo: als dubbele kans op roem, dan die anders beoordelen, dan enkele kans op roem
    //      bijv. 7,9 ==> dan is er met 8 kans op 20 roem
    //            8,9 ==> dan is er met 7 en 10 kans op 20 roem

    protected fun roemSureThisTrickByCandidate(candidate: Card): Int {
        return (currentTrick.getCardsPlayed() + candidate).bonusValue(trump)
    }

    protected fun roemPossibleThisTrickByCandidate(candidate: Card): Int {

        val listOfTrickPossibilities = if (iAmSecondPlayer) {
            val cardsPlayer1 = player1.legalCards
            val cardsPlayer2 = player2.legalCards
            if (cardsPlayer1.size > 1 && cardsPlayer2.size > 1) {
                (cardsPlayer1 + cardsPlayer2).toList().mapCombinedItems { card1, card2 -> (currentTrick.getCardsPlayed() + candidate + card1 + card2) }
            } else {
                (cardsPlayer1 + cardsPlayer2).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
            }
        } else if (iAmThirdPlayer) {
            val cardsPlayer1 = player1.legalCards
            (cardsPlayer1).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
        } else { //iAmFourthPlayer
            listOf((currentTrick.getCardsPlayed() + candidate))
        }
        return listOfTrickPossibilities
            .filter{ poss -> poss.bonusValue(trump) > (poss-candidate).bonusValue(trump)}
            .maxOfOrNull { poss -> poss.bonusValue(trump) }
            ?: 0
    }

    protected fun isRoemPossibleNextTrick(candidate: Card): Boolean {
        val p1 = player1.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p2 = player2.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p3 = player3.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val doHave = p1.size.sign + p2.size.sign + p3.size.sign
        if (doHave <= 1)
            return false
        val all = p1 + p2 + p3 + candidate
        if (all.size <= 2)
            return false
        return all.maxSequenceUsing(candidate) >=3
    }

//------------------------------------------------------------------------------------------------------------------

    protected fun cardGivingBestValueByPlayingFullTrick(): Card {
        val result = cardGivingBestValueByPlayingFullTrick(currentTrick, mySide)
        return result.card?:throw Exception("card givng best value surprsingly gave null result")
    }

    private data class CardOrNullValue (val card: Card?, val value: Int)
    private fun cardGivingBestValueByPlayingFullTrick(trick: Trick, sideToMove: TableSide): CardOrNullValue {
        if (trick.isComplete())
            return CardOrNullValue(null, trick.getScore().getDeltaForPlayer(mySide))

        val checkCards = if (sideToMove == mySide) myLegalCards else player(sideToMove).legalCards.toList()

        if (sideToMove == mySide || sideToMove.isPartner()) {
            var best = CardOrNullValue(null, Int.MIN_VALUE)
            checkCards.forEach { card ->
                trick.addCard(card)
                val cv = cardGivingBestValueByPlayingFullTrick(trick, sideToMove.clockwiseNext())
                trick.removeLastCard()
                if (cv.value > best.value)
                    best = CardOrNullValue(card, cv.value)
            }
            return best
        } else {
            var best = CardOrNullValue(null, Int.MAX_VALUE)
            checkCards.forEach { card ->
                trick.addCard(card)
                val cv = cardGivingBestValueByPlayingFullTrick(trick, sideToMove.clockwiseNext())
                trick.removeLastCard()
                if (cv.value < best.value)
                    best = CardOrNullValue(card, cv.value)
            }
            return best
        }
    }

    companion object {
        var doPrintFallBack = true
    }

}

