package com.cards.player.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.game.fourplayercardgame.klaverjassen.GameKlaverjassen
import com.cards.game.fourplayercardgame.klaverjassen.RoundKlaverjassen
import com.cards.game.fourplayercardgame.klaverjassen.legalPlayable
import com.cards.player.Player

open class PlayerKlaverjassen(tableSide: TableSide, game: GameKlaverjassen) : Player(tableSide, game) {

    fun getCurrentRound() = game.getCurrentRound() as RoundKlaverjassen

    override fun chooseCard(): Card {
        return getCardsInHand()
            .legalPlayable(
                getCurrentRound().getTrickOnTable().getCardsPlayed(),
                getCurrentRound().getTrumpColor())
            .first()
    }

    open fun chooseTrumpColor(cardColorOptions: List<CardColor> = CardColor.values().toList()): CardColor {
        return cardColorOptions[tableSide.ordinal % cardColorOptions.size]
    }
}