package com.cards.player

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.legalPlayable

open class Player(
    val tableSide: TableSide,
    val game: Game) {

    private val cardsInHand = mutableListOf<Card>()

    fun getCardsInHand() = cardsInHand.toList()
    fun getNumberOfCardsInHand() = getCardsInHand().size

    fun setCardsInHand(cardsFromDealer: List<Card>) {
        cardsInHand.clear()
        cardsInHand.addAll(cardsFromDealer.toMutableList())
    }

    fun addCard(card: Card) {
        if (card in cardsInHand) {
            throw Exception("try to add card $card to hand of player $tableSide, but it is already there")
        }
        cardsInHand.add(card)
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

    fun getLegalPlayableCards(): List<Card> {
        return if (game.newRoundToBeStarted())
            emptyList() //trump is not known yet, so no idea what will be legal
        else
            getCardsInHand()
                .legalPlayable(game.getCurrentRound().getTrickOnTable(), game.getCurrentRound().getTrumpColor())
    }


    open fun chooseCard(): Card {
        return getLegalPlayableCards().first()
    }

    open fun chooseTrumpColor(cardColorOptions: List<CardColor> = CardColor.values().toList()): CardColor {
        return cardColorOptions[tableSide.ordinal % cardColorOptions.size]
    }

}