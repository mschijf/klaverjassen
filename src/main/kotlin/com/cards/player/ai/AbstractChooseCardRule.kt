package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.*
import com.cards.player.Player
import tool.mylambdas.collectioncombination.mapCombinedItems
import kotlin.math.sign

abstract class AbstractChooseCardRule(protected val player: Player,
                                      val brainDump: BrainDump) {
    abstract fun chooseCard(): Card

    val myLegalCards = player.getLegalPlayableCards()
    val myCardsInHand = player.getCardsInHand()
    val mySide = player.tableSide
    val myLegalCardsByColor = myLegalCards.groupBy { it.color }

    protected val currentGame = player.game
    protected val currentRound = currentGame.getCurrentRound()
    protected val currentTrick = currentRound.getTrickOnTable()

    private val colorPlayed = brainDump.cardsPlayed.groupingBy { it.color }.eachCount()
    protected fun CardColor.colorPlayedCount() = colorPlayed[this]?:0

    protected fun CardColor.isTrump() = this == brainDump.trump
    protected fun Card.isTrump() = this.color.isTrump()

    protected fun TableSide.isPartner() = this == brainDump.partner
    protected fun TableSide.isOtherParty() = this == brainDump.p1 || this == brainDump.p3
    protected fun TableSide.isContractOwner() = this == brainDump.contractOwner

    protected fun Card.isKaal() = myLegalCardsByColor[this.color]!!.size == 1
    protected fun Card.isVrij() = brainDump.cardsInPlayOtherPlayers.none { it.color == this.color }

    //todo: check all highest algorithms
    protected fun Card.isHigherThanOtherInPlay() = brainDump.cardsInPlayOtherPlayers.none { it.color == this.color && it.beats(this, brainDump.trump)}
    protected fun Card.isHigherThanAllInPlayIncludingMine() = brainDump.allCardsInPlay.none { it.color == this.color && it.beats(this, brainDump.trump)}

    protected fun CardColor.highestInPlayOrOnTable() =
        (brainDump.cardsInPlayOtherPlayers + currentTrick.getCardsPlayed()).filter {it.color == this}.maxByOrNull { it.toRankNumber(brainDump.trump) }

    protected fun CardColor.myHighest() = myLegalCardsByColor[this]?.maxBy { it.toRankNumber(brainDump.trump) }
    protected fun CardColor.iHaveHighest() = myHighest()?.isHigherThanOtherInPlay()?:false

    protected fun CardColor.playedForFirstTime() = currentTrick.isLeadColor(this) &&
            player.game.getCurrentRound().getTrickList().dropLast(1).count{it.isLeadColor(this)} == 0
    protected fun CardColor.playedForSecondTime() = currentTrick.isLeadColor(this) &&
            player.game.getCurrentRound().getTrickList().dropLast(1).count{it.isLeadColor(this)} == 1
    protected fun CardColor.playedBefore() = !playedForFirstTime()

    protected fun Card.cardValue() = this.cardValue(brainDump.trump)
    protected fun hasCard(card: Card) = card in player.getCardsInHand()

    protected val trumpJack = Card(brainDump.trump, CardRank.JACK)
    protected val trumpNine = Card(brainDump.trump, CardRank.NINE)
    protected val trumpAce = Card(brainDump.trump, CardRank.ACE)
    protected val trumpTen = Card(brainDump.trump, CardRank.ACE)


    //------------------------------------------------------------------------------------------------------------------

    protected fun playFallbackCard(info: String? = null): Card {
//        if (info != null)
//            println("FALL BACK NOTE: Fallback card info: $info")
        return myLegalCards.first()
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    //todo: als dubbele kans op roem, dan die anders beoordelen, dan enkele kans op roem
    //      bijv. 7,9 ==> dan is er met 8 kans op 20 roem
    //            8,9 ==> dan is er met 7 en 10 kans op 20 roem

    protected fun roemSureThisTrickByCandidate(candidate: Card): Int {
        return (currentTrick.getCardsPlayed() + candidate).bonusValue(brainDump.trump)
    }

    protected fun roemPossibleThisTrickByCandidate(candidate: Card): Int {

        val listOfTrickPossibilities = if (brainDump.iAmSecondPlayer) {
            val cardsPlayer1 = brainDump.player1.legalCards
            val cardsPlayer2 = brainDump.player2.legalCards
            if (cardsPlayer1.size > 1 && cardsPlayer2.size > 1) {
                (cardsPlayer1 + cardsPlayer2).toList().mapCombinedItems { card1, card2 -> (currentTrick.getCardsPlayed() + candidate + card1 + card2) }
            } else {
                (cardsPlayer1 + cardsPlayer2).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
            }
        } else if (brainDump.iAmThirdPlayer) {
            val cardsPlayer1 = brainDump.player1.legalCards
            (cardsPlayer1).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
        } else { //iAmFourthPlayer
            listOf((currentTrick.getCardsPlayed() + candidate))
        }
        return listOfTrickPossibilities.maxOf { poss -> poss.bonusValue(brainDump.trump) }
    }

    protected fun isRoemPossibleNextTrick(candidate: Card): Boolean {
        val p1 = brainDump.player1.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p2 = brainDump.player2.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p3 = brainDump.player3.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
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

