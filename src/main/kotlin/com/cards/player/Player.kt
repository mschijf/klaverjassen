package com.cards.player

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.legalPlayable

open class Player(
    val tableSide: TableSide,
    val game: Game) {

    private var cardsInHand: MutableList<Card> = mutableListOf()

    fun getCardsInHand() = cardsInHand.toList()
    fun getNumberOfCardsInHand() = getCardsInHand().size

    fun setCardsInHand(cardsFromDealer: List<Card>) {
        cardsInHand = cardsFromDealer.toMutableList()
    }

    fun removeCard(card: Card) {
        if (!cardsInHand.remove(card)) {
            throw Exception("cannot remove card $card from hand of player $tableSide ")
        }
    }

    override fun toString() = "pl-$tableSide"

    //------------------------------------------------------------------------------------------------------------------
    // Klaverjassen specific
    //------------------------------------------------------------------------------------------------------------------

    open fun chooseCard(): Card {
        return getCardsInHand()
            .legalPlayable(
                game.getCurrentRound().getTrickOnTable().getCardsPlayed(),
                game.getCurrentRound().getTrumpColor())
            .first()
    }

    open fun chooseTrumpColor(cardColorOptions: List<CardColor> = CardColor.values().toList()): CardColor {
        return cardColorOptions[tableSide.ordinal % cardColorOptions.size]
    }

}