package com.cards.player.hearts

import com.cards.game.card.Card
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.game.fourplayercardgame.hearts.GameHearts
import com.cards.game.fourplayercardgame.hearts.legalPlayable
import com.cards.player.Player

open class PlayerHearts (tableSide: TableSide, game: GameHearts) : Player(tableSide, game) {

    override fun chooseCard(): Card {
        return getCardsInHand()
            .legalPlayable(
                game.getCurrentRound().getTrickOnTable().getCardsPlayed()
            )
        .first()
    }
}