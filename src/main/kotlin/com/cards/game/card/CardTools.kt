package com.cards.game.card

fun List<Card>.ofColor(cardColor: CardColor) = this.filter { card -> card.color == cardColor }
