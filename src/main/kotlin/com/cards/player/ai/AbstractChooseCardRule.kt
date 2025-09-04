package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.beats
import com.cards.player.Player

abstract class AbstractPlayerRules(protected val player: Player,
                                   val brainDump: BrainDump) {
    abstract fun chooseCard(): Card

    val myLegalCards = player.getLegalPlayableCards()
    val myCardsInHand = player.getCardsInHand()
    val mySide = player.tableSide
    val myLegalCardsByColor = myLegalCards.groupBy { it.color }

    val currentRound = player.game.getCurrentRound()
    val currentTrick = currentRound.getTrickOnTable()

    protected fun CardColor.isTrump() = this == brainDump.trump
    protected fun Card.isTrump() = this.color.isTrump()

    protected fun TableSide.isPartner() = this == brainDump.partner
    protected fun TableSide.isOtherParty() = this == brainDump.p1 || this == brainDump.p3

    protected fun Card.isKaal() = myLegalCardsByColor[this.color]!!.size == 1
    protected fun Card.isVrij() = brainDump.cardsInPlayOtherPlayers.none { it.color == this.color }
    protected fun Card.isHighestInPlay() = brainDump.cardsInPlayOtherPlayers.none { it.color == this.color && it.beats(this, brainDump.trump)}

    protected fun hasCard(card: Card) = card in player.getCardsInHand()
    protected fun trumpJack() = Card(brainDump.trump, CardRank.JACK)
    protected fun trumpNine() = Card(brainDump.trump, CardRank.NINE)

    //------------------------------------------------------------------------------------------------------------------

    protected fun playFallbackCard(info: String? = null): Card {
//        if (info != null)
//            println("FALL BACK NOTE: Fallback card info: $info")
        return myLegalCards.first()
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

