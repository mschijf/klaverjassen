package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player

abstract class AbstractPlayerInTrick(protected val player: Player,
                                     val analysis: KlaverjassenAnalysisResult) {
    abstract fun chooseCard(): Card

    protected fun CardColor.isTrump() = this == analysis.trump
    protected fun Card.isTrump() = this.color.isTrump()

    protected fun TableSide.isPartner() = this == analysis.partner
    protected fun TableSide.isOtherParty() = this == analysis.player1 || this == analysis.player3

    protected fun hasCard(card: Card) = card in player.getCardsInHand()
    protected fun trumpJack() = Card(analysis.trump, CardRank.JACK)
    protected fun trumpNine() = Card(analysis.trump, CardRank.NINE)

    //------------------------------------------------------------------------------------------------------------------
}

