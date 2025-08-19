package com.cards.player.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.legalPlayable
import com.cards.player.Player

open class PlayerKlaverjassen(tableSide: TableSide, game: Game) : Player(tableSide, game) {

    override fun chooseCard(): Card {
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