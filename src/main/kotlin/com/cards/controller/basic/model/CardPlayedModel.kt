package com.cards.controller.basic.model

import com.cards.game.card.Card
import com.cards.game.fourplayercardgame.basic.TableSide

data class CardPlayedModel(
    val player: TableSide,
    val cardPlayed: Card,
    val nextPlayer: TableSide,
    val cardsInHand: Int,
    val trickCompleted: TrickCompletedModel?)

data class TrickCompletedModel(val trickWinner: TableSide, val roundCompleted: Boolean, val gameOver: Boolean)