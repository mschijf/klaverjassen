package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank

class BruteForce(
    val playerToMove: GeniusPlayerKlaverjassen,
    val analyzer: KlaverjassenAnalyzer) {

    fun mostValuableCardToPlay(): Card {
        return Card(CardColor.HEARTS, CardRank.QUEEN)
    }

}