package com.cards.controller.basic.model

import com.cards.game.card.Card

data class TableModel(
    val south: Card?,
    val west: Card?,
    val north: Card?,
    val east: Card?)

