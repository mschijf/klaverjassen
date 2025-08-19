package com.cards.player.klaverjassen.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank

class TrumpChoiceAnalyzer(private val cardList: List<Card>) {

    fun trumpChoiceValue(cardColor: CardColor): Int {
        var value = 0
        val trumpCardRanks = cardList.filter { it.color == cardColor }.map { it.rank }
        value += if (CardRank.JACK in trumpCardRanks) 80 else 0
        value +=
            if (CardRank.NINE in trumpCardRanks)
                if (CardRank.JACK in trumpCardRanks)
                    50
                else if (trumpCardRanks.size > 1)
                    20 + trumpCardRanks.size * 10
                else
                    0
            else
                0
        value +=
            if (CardRank.ACE in trumpCardRanks)
                if (trumpCardRanks.size > 1)
                    10
                else
                    0
            else
                0
        value += when (trumpCardRanks.size) {
            2 -> 5
            3 -> 10
            4 -> 30
            5 -> 100
            6, 7, 8 -> 200
            else -> 0
        }

        (CardColor.values().toSet() - cardColor).forEach { noTrumpColor ->
            val noTrumpCardRanks = cardList.filter { it.color == noTrumpColor }.map { it.rank }
            value += if (CardRank.ACE in noTrumpCardRanks) 20 else 0
            value += if (CardRank.TEN in noTrumpCardRanks && CardRank.ACE in noTrumpCardRanks) 10 else 0
            value += if (CardRank.TEN in noTrumpCardRanks && noTrumpCardRanks.size == 2) 5 else 0
        }
        return value
    }
}