package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.*
import com.cards.player.Player
import tool.mylambdas.collectioncombination.mapCombinedItems
import kotlin.collections.filter
import kotlin.math.sign

abstract class AbstractChooseCardRule(protected val player: Player) {

    abstract fun chooseCard(): Card

    protected val memory = Memory.refresh(player)

    protected val currentGame = player.game
    protected val currentRound = currentGame.getCurrentRound()
    protected val currentTrick = currentRound.getTrickOnTable()
    protected val trump = player.game.getCurrentRound().getTrumpColor()
    protected val contractOwner = player.game.getCurrentRound().getContractOwningSide()

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

    protected val p1 = player.tableSide.clockwiseNext(1)
    protected val p2 = player.tableSide.clockwiseNext(2)
    protected val p3 = player.tableSide.clockwiseNext(3)
    protected val partner = p2

    protected val nTrickCardsPlayed = currentTrick.getCardsPlayed().size
    protected val iAmFirstPlayer = nTrickCardsPlayed == 0
    protected val iAmSecondPlayer = nTrickCardsPlayed == 1
    protected val iAmThirdPlayer = nTrickCardsPlayed == 2
    protected val iAmFourthPlayer = nTrickCardsPlayed == 3

    protected val theyOwnContract = contractOwner == p1 || contractOwner == p3
    protected val iAmContractOwnersPartner = contractOwner == p2
    protected val iAmContractOwner = !theyOwnContract && !iAmContractOwnersPartner

    private val playerMap = memory.playerList.associateBy { it.tableSide }
    protected fun player(side: TableSide) = playerMap[side]?:throw Exception("asked for wrong player")
    protected val player1 = playerMap[p1]!!
    protected val player2 = playerMap[p2]!!
    protected val player3 = playerMap[p3]!!

    protected val partnerCards = player(p2).allAssumeCards
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
    protected fun TableSide.isOtherParty() = this == p1 || this == p3
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
    protected fun troefGetrokken() = currentRound.getTrickList().dropLast(1).any { it.isLeadColor(trump)}
    protected fun othersCanHaveTroef() = memory.cardsInPlayOtherPlayers.any { it.isTrump() }
    protected fun opponentCanHaveTroef() = (player1.allAssumeCards + player3.allAssumeCards).any { it.isTrump() }
    protected fun partnerCanHaveTroef() = player2.allAssumeCards.any { it.isTrump() }


    //------------------------------------------------------------------------------------------------------------------

    protected fun playFallbackCard(info: String? = null): Card {
//        if (info != null)
//            println("FALL BACK NOTE: Fallback card info: $info")
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
        return listOfTrickPossibilities.maxOf { poss -> poss.bonusValue(trump) }
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
//    private fun List<Card>.cardGivingBestValue(): CardValue {
//        var best = Int.MIN_VALUE
//        var bestCard: Card? = null
//        this.forEach { card ->
//            val v = card.trickValueAfterPlayed()
//            if (v > best) {
//                best = v
//                bestCard = card
//            }
//        }
//        return CardValue(bestCard!!, best)
//    }
//
//    private fun Card.trickValueAfterPlayed(): Int {
//        currentTrick.addCard(this)
//        val v = currentTrick.getScore().getDeltaForPlayer(player.tableSide)
//        currentTrick.removeLastCard()
//        return v
//    }
//
//
//    private val dummyCard = Card(CardColor.CLUBS, CardRank.THREE)
//    private fun cardGivingBestValueByPlayingFullTrick(trick: Trick, sideToMove: TableSide): CardValue {
//        if (trick.isComplete())
//            return CardValue(dummyCard, trick.getScore().getDeltaForPlayer(player.tableSide))
//
//        val checkCards = if (sideToMove == player.tableSide) {
//            player.getLegalPlayableCards()
//        }  else {
//            (brain.player(sideToMove).allAssumeCards - trick.getCardsPlayed())
//                .toList()
//                .legalPlayable(trick, brain.trump)
//        }
//
//        if (sideToMove == player.tableSide || sideToMove.opposite() == player.tableSide) {
//            var best = CardValue(dummyCard, Int.MIN_VALUE)
//            checkCards.forEach { card ->
//                trick.addCard(card)
//                val cv = cardGivingBestValueByPlayingFullTrick(trick, sideToMove.clockwiseNext())
//                trick.removeLastCard()
//                if (cv.value > best.value)
//                    best = CardValue(card, cv.value)
//            }
//            return best
//        } else {
//            var best = CardValue(dummyCard, Int.MAX_VALUE)
//            checkCards.forEach { card ->
//                trick.addCard(card)
//                val cv = cardGivingBestValueByPlayingFullTrick(trick, sideToMove.clockwiseNext())
//                trick.removeLastCard()
//                if (cv.value < best.value)
//                    best = CardValue(card, cv.value)
//            }
//            return best
//        }
//    }

}

