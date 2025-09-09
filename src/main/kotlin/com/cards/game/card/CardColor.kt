package com.cards.game.card

enum class CardColor(val colorString: String) {
    SPADES("S"),
    HEARTS("H"),
    CLUBS("C"),
    DIAMONDS("D");

    fun otherColors(): Set<CardColor> = values().toSet() - this
}
