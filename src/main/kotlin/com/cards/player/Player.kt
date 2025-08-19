package com.cards.player

import com.cards.game.card.Card
import com.cards.game.fourplayercardgame.basic.Game
import com.cards.game.fourplayercardgame.basic.TableSide

abstract class Player(
    val tableSide: TableSide,
    protected val game: Game
) {

    private var cardsInHand: MutableList<Card> = mutableListOf()

    abstract fun chooseCard(): Card

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
}