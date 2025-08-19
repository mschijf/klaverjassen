package com.cards.game.klaverjassen.basic

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.klaverjassen.ScoreKlaverjassen
import com.cards.game.klaverjassen.klaverjassen.bonusValue
import com.cards.game.klaverjassen.klaverjassen.cardValue
import com.cards.game.klaverjassen.klaverjassen.toRankNumberNoTrump
import com.cards.game.klaverjassen.klaverjassen.toRankNumberTrump

class Trick(
    private val sideToLead: TableSide,
    private val round: Round) {

    private val cardsPlayed = mutableListOf<Card>()

    fun getSideToLead() = sideToLead
    fun isSideToLead(side: TableSide) = getSideToLead() == side
    fun getSideToPlay() = sideToLead.clockwiseNext(cardsPlayed.size)

    fun getLeadColor() = cardsPlayed.firstOrNull()?.color
    fun isLeadColor(color: CardColor) = color == getLeadColor()

    fun getCardsPlayed() = cardsPlayed.toList()
    fun getSidesPlayed() = cardsPlayed.mapIndexed { index, _ -> sideToLead.clockwiseNext(index) }

    fun hasNotStarted() = cardsPlayed.isEmpty()
    fun isActive() = !isComplete()
    fun isComplete() = cardsPlayed.size == TableSide.values().size

    fun getCardPlayedBy(tableSide: TableSide): Card? {
        val distance = sideToLead.clockwiseDistanceFrom(tableSide)
        return cardsPlayed.elementAtOrNull(distance)
    }

    fun getSideThatPlayedCard(card: Card?): TableSide? {
        val index = getCardsPlayed().indexOf(card)
        return if (index >= 0) sideToLead.clockwiseNext(index) else null
    }

    fun addCard(aCard: Card) {
        if (isComplete())
            throw Exception("Adding a card to a completed trick")
        cardsPlayed.add(aCard)
    }

    fun removeLastCard() {
        if (hasNotStarted())
            throw Exception("Removing a card from a not started trick")
        cardsPlayed.removeLast()
    }

    //------------------------------------------------------------------------------------------------------------------
    // Klaverjassen specific
    //------------------------------------------------------------------------------------------------------------------

    fun getWinningSide(): TableSide? {
        return getSideThatPlayedCard(getWinningCard())
    }

    fun getWinningCard(): Card? {
        return if (getCardsPlayed().any { card -> card.color == round.getTrumpColor() }) {
            getCardsPlayed()
                .filter { card -> card.color == round.getTrumpColor() }
                .maxByOrNull { card -> card.toRankNumberTrump() }
        } else {
            getCardsPlayed()
                .filter { card -> isLeadColor(card.color) }
                .maxByOrNull { card -> card.toRankNumberNoTrump() }
        }
    }

    fun getScore(): ScoreKlaverjassen {
        val lastTrickPoints = if (round.isLastTrick(this)) 10 else 0
        return if (!isComplete()) {
            ScoreKlaverjassen.ZERO
        } else {
            ScoreKlaverjassen.scoreForPlayer(
                getWinningSide()!!,
                lastTrickPoints + getCardsPlayed().sumOf { card -> card.cardValue(round.getTrumpColor()) },
                getCardsPlayed().bonusValue(trumpColor = round.getTrumpColor())
            )
        }
    }

}