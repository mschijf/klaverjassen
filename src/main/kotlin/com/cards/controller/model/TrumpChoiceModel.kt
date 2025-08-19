package com.cards.controller.model

import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.basic.TableSide

data class TrumpChoiceModel(
    val trumpColor: CardColor,
    val contractOwner: TableSide,
)
