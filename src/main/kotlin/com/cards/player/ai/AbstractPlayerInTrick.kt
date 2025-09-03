package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player

abstract class AbstractPlayerInTrick(protected val player: Player,
                                     val brain: Brain) {
    abstract fun chooseCard(): Card

    protected fun CardColor.isTrump() = this == brain.trump
    protected fun Card.isTrump() = this.color.isTrump()

    protected fun TableSide.isPartner() = this == brain.partner
    protected fun TableSide.isOtherParty() = this == brain.p1 || this == brain.p3

    protected fun hasCard(card: Card) = card in player.getCardsInHand()
    protected fun trumpJack() = Card(brain.trump, CardRank.JACK)
    protected fun trumpNine() = Card(brain.trump, CardRank.NINE)

    //------------------------------------------------------------------------------------------------------------------
}

