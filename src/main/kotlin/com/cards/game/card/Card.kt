package com.cards.game.card

data class Card(val color: CardColor, val rank: CardRank) {
    override fun toString(): String {
        return rank.rankString + color.colorString
    }

    companion object {
        fun of(cardString: String): Card {
            if (cardString.length != 2 && cardString.length != 3)
                throw IllegalArgumentException("The cardString must have exactly 2 or 3 characters")
            val cardRank = CardRank.values().firstOrNull { it.rankString == cardString.dropLast(1) }
            val cardColor = CardColor.values().firstOrNull { it.colorString == cardString.takeLast(1) }
            if (cardRank == null || cardColor == null)
                throw IllegalArgumentException("Illegal card string")
            return Card(cardColor, cardRank)
        }
    }

    fun isHearts() = color == CardColor.HEARTS
    fun isSpades() = color == CardColor.SPADES
    fun isClubs() = color == CardColor.CLUBS
    fun isDiamonds() = color == CardColor.DIAMONDS

    fun isTwo(cardColor: CardColor? = null) = rank == CardRank.TWO && (if(cardColor != null) cardColor == color else true)
    fun isThree(cardColor: CardColor? = null) = rank == CardRank.THREE && (if(cardColor != null) cardColor == color else true)
    fun isFour(cardColor: CardColor? = null) = rank == CardRank.FOUR && (if(cardColor != null) cardColor == color else true)
    fun isFive(cardColor: CardColor? = null) = rank == CardRank.FIVE && (if(cardColor != null) cardColor == color else true)
    fun isSix(cardColor: CardColor? = null) = rank == CardRank.SIX && (if(cardColor != null) cardColor == color else true)
    fun isSeven(cardColor: CardColor? = null) = rank == CardRank.SEVEN && (if(cardColor != null) cardColor == color else true)
    fun isEight(cardColor: CardColor? = null) = rank == CardRank.EIGHT && (if(cardColor != null) cardColor == color else true)
    fun isNine(cardColor: CardColor? = null) = rank == CardRank.NINE && (if(cardColor != null) cardColor == color else true)
    fun isTen(cardColor: CardColor? = null) = rank == CardRank.TEN && (if(cardColor != null) cardColor == color else true)
    fun isJack(cardColor: CardColor? = null) = rank == CardRank.JACK && (if(cardColor != null) cardColor == color else true)
    fun isQueen(cardColor: CardColor? = null) = rank == CardRank.QUEEN && (if(cardColor != null) cardColor == color else true)
    fun isKing(cardColor: CardColor? = null) = rank == CardRank.KING && (if(cardColor != null) cardColor == color else true)
    fun isAce(cardColor: CardColor? = null) = rank == CardRank.ACE && (if(cardColor != null) cardColor == color else true)
}