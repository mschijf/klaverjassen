package com.cards.game.fourplayercardgame.basic

import com.cards.game.card.Card
import com.cards.game.card.CardColor

abstract class Trick(
    private val sideToLead: TableSide) {

    private val cardsPlayed = mutableListOf<Card>()

    abstract fun getWinningSide(): TableSide?
    abstract fun getWinningCard(): Card?

    fun getSideToLead() = sideToLead
    fun isSideToLead(side: TableSide) = getSideToLead() == side
    fun isLastSideToPlay(side: TableSide) = side.clockwiseDistanceFrom(sideToLead) == 3
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
}