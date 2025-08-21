package com.cards.player

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.Trick
import com.cards.game.klaverjassen.toRankNumberTrump

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
        return getCardsInHand()
            .legalPlayable(game.getCurrentRound().getTrickOnTable(), game.getCurrentRound().getTrumpColor())
    }

    private fun List<Card>.legalPlayable(trick: Trick, trumpColor: CardColor): List<Card> {
        val cardsPlayed = trick.getCardsPlayed()
        if (cardsPlayed.isEmpty())
            return this

        val leadColor = cardsPlayed.first().color
        if (this.any {card -> card.color == leadColor}) {
            return if (trumpColor == leadColor) {
                this.legalTrumpCardsToPlay(cardsPlayed, trumpColor).ifEmpty { this }
            } else {
                this.filter { card -> card.color == leadColor }.ifEmpty { this }
            }
        }

        if (this.any {card -> card.color == trumpColor}) {
            return this.legalTrumpCardsToPlay(cardsPlayed, trumpColor)
        }

        return this
    }

    private fun highestTrumpCard(cardsPlayed: List<Card>, trumpColor: CardColor) : Card? {
        return cardsPlayed
            .filter{ cardPlayed -> cardPlayed.color == trumpColor }
            .maxByOrNull { cardPlayed -> cardPlayed.toRankNumberTrump() }
    }

    private fun List<Card>.legalTrumpCardsToPlay(cardsPlayed: List<Card>, trumpColor: CardColor):List<Card> {
        val highestTrumpCard = highestTrumpCard(cardsPlayed, trumpColor)
        val maxTrumpCardRank = highestTrumpCard?.toRankNumberTrump() ?: Int.MAX_VALUE

        return this
            .filter { card -> (card.color == trumpColor) && card.toRankNumberTrump() > maxTrumpCardRank }
            .ifEmpty { this.filter { card -> card.color == trumpColor } }
    }


    open fun chooseCard(): Card {
        return getLegalPlayableCards().first()
    }

    open fun chooseTrumpColor(cardColorOptions: List<CardColor> = CardColor.values().toList()): CardColor {
        return cardColorOptions[tableSide.ordinal % cardColorOptions.size]
    }

}