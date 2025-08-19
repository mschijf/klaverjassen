package com.cards.controller.basic.model

import com.cards.game.card.Card
import com.cards.game.fourplayercardgame.basic.TableSide

data class GameStatusModel(
    val onTable: TableModel,
    val playerToMove: TableSide,
    val leadPlayer: TableSide,
    val newRoundStarted: Boolean = false,
    val playerSouth: List<CardInHandModel>,
    val playerWest: List<CardInHandModel>,
    val playerNorth: List<CardInHandModel>,
    val playerEast: List<CardInHandModel>,
    val gameJsonString: String,
    val seed: Int
)

data class CardInHandModel(
    val card: Card,
    val playable: Boolean,
    val geniusValue: String)
