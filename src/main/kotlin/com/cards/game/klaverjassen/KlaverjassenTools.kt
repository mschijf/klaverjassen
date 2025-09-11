package com.cards.game.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import kotlin.collections.ifEmpty
import kotlin.math.max

const val NUMBER_OF_TRICKS_PER_ROUND = 8
const val NUMBER_OF_ROUNDS_PER_GAME = 16
const val PIT_BONUS = 100
val GAME_START_PLAYER = TableSide.WEST

fun Card.beats(other: Card?, trumpColor: CardColor): Boolean {
    if (other == null)
        return true
    return if (this.color == other.color) {
        if (this.color == trumpColor) {
            this.toRankNumberTrump() > other.toRankNumberTrump()
        } else {
            this.toRankNumberNoTrump() > other.toRankNumberNoTrump()
        }
    } else {
        (other.color != trumpColor)
    }
}

fun Card.cardValue(trump: CardColor): Int {
    return when (this.rank) {
        CardRank.ACE -> 11
        CardRank.TEN -> 10
        CardRank.KING -> 4
        CardRank.QUEEN -> 3
        CardRank.JACK -> if (this.color == trump) 20 else 2
        CardRank.NINE -> if (this.color == trump) 14 else 0
        CardRank.EIGHT -> 0
        CardRank.SEVEN -> 0
        else -> 0
    }
}

fun Card.toRankNumber (trumpColor: CardColor) =
    if (this.color == trumpColor)
        this.toRankNumberTrump()
    else
        this.toRankNumberNoTrump()

fun Card.toRankNumberNoTrump () : Int {
    return when(this.rank) {
        CardRank.ACE -> 111
        CardRank.TEN -> 110
        CardRank.KING -> 104
        CardRank.QUEEN -> 103
        CardRank.JACK -> 102
        CardRank.NINE -> 9
        CardRank.EIGHT -> 8
        CardRank.SEVEN -> 7
        else -> 0
    }
}

fun Card.toRankNumberTrump () : Int {
    return when(this.rank) {
        CardRank.JACK -> 220
        CardRank.NINE -> 214
        CardRank.ACE -> 111
        CardRank.TEN -> 110
        CardRank.KING -> 104
        CardRank.QUEEN -> 103
        CardRank.EIGHT -> 8
        CardRank.SEVEN -> 7
        else -> 0
    }
}

fun Card.toBonusRankNumber() : Int {
    return this.rank.rankNumber
}

fun List<Card>.bonusValue(trumpColor: CardColor): Int {
    return CardColor.values().sumOf { color -> bonusValueForColor(this, color, trumpColor) } +
            bonusValueForfourEqualRanks(this)
}

private fun bonusValueForfourEqualRanks(cardList: List<Card>): Int {
    //see for rules: https://www.spelregels.eu/wp-content/uploads/2021/01/spelregels-klaverjassen.pdf
    return if (cardList.size == 4 && cardList.map {it.rank}.distinct().size == 1)
        100
    else
        0
}

private fun bonusValueForColor(cardList: List<Card>, forCardColor: CardColor, trumpColor: CardColor): Int {
    val checkList = cardList.filter { card -> card.color == forCardColor }.sortedBy { card -> card.rank }
    val stuk = if (forCardColor == trumpColor && checkList.any { card -> card.rank == CardRank.QUEEN } && checkList.any { card -> card.rank == CardRank.KING }) {
        20
    } else {
        0
    }
    val bonus = when (checkList.size) {
        3 -> if (checkList[2].toBonusRankNumber() - checkList[0].toBonusRankNumber() == 2) 20 else 0
        4 ->
            if (checkList[3].toBonusRankNumber() - checkList[0].toBonusRankNumber() == 3)
                50
            else if (checkList[2].toBonusRankNumber() - checkList[0].toBonusRankNumber() == 2)
                20
            else if (checkList[3].toBonusRankNumber() - checkList[1].toBonusRankNumber() == 2)
                20
            else
                0
        else -> 0
    }
    return bonus + stuk
}

//----------------------------------------------------------------------------------------------------------------------

fun Collection<Card>.legalPlayable(trick: Trick, trumpColor: CardColor): List<Card> {
    val cardsPlayed = trick.getCardsPlayed()
    if (cardsPlayed.isEmpty())
        return this.toList()

    val leadColor = cardsPlayed.first().color
    if (this.any {card -> card.color == leadColor}) {
        return if (trumpColor == leadColor) {
            this.legalTrumpCardsToPlay(cardsPlayed, trumpColor).ifEmpty { this.toList() }
        } else {
            this.filter { card -> card.color == leadColor }.ifEmpty { this.toList() }
        }
    }

    if (this.any {card -> card.color == trumpColor}) {
        return this.legalTrumpCardsToPlay(cardsPlayed, trumpColor)
    }

    return this.toList()
}

private fun highestTrumpCard(cardsPlayed: List<Card>, trumpColor: CardColor) : Card? {
    return cardsPlayed
        .filter{ cardPlayed -> cardPlayed.color == trumpColor }
        .maxByOrNull { cardPlayed -> cardPlayed.toRankNumberTrump() }
}

private fun Collection<Card>.legalTrumpCardsToPlay(cardsPlayed: List<Card>, trumpColor: CardColor):List<Card> {
    val highestTrumpCard = highestTrumpCard(cardsPlayed, trumpColor)
    val maxTrumpCardRank = highestTrumpCard?.toRankNumberTrump() ?: Int.MAX_VALUE

    return this
        .filter { card -> (card.color == trumpColor) && card.toRankNumberTrump() > maxTrumpCardRank }
        .ifEmpty { this.filter { card -> card.color == trumpColor } }
}

//----------------------------------------------------------------------------------------------------------------------

fun Collection<Card>.maxSequenceUsing(usingCard:Card): Int {
    var currentSequence = 0
    var maxSequence = 0
    var lastCardRank = -1000
    var usingCardUsed = false
    this.sortedBy { it.toBonusRankNumber() }.forEach { card ->
        if (card.toBonusRankNumber() == lastCardRank+1) {
            currentSequence++
            if (card == usingCard)
                usingCardUsed = true
        } else {
            currentSequence = 1
            usingCardUsed = card == usingCard
        }
        if (usingCardUsed)
            maxSequence = max(maxSequence, currentSequence)
        lastCardRank = card.toBonusRankNumber()
    }
    return maxSequence
}

fun Collection<Card>.maxSequence(): Int {
    var currentSequence = 0
    var maxSequence = 0
    var lastCardRank = -1000
    this.sortedBy { it.toBonusRankNumber() }.forEach { card ->
        if (card.toBonusRankNumber() == lastCardRank+1) {
            currentSequence++
        } else {
            currentSequence = 1
        }
        maxSequence = max(maxSequence, currentSequence)
        lastCardRank = card.toBonusRankNumber()
    }
    return maxSequence
}