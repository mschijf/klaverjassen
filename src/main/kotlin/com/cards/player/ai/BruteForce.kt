package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.GameStatus
import com.cards.player.Player

class BruteForce(
    val playerToMove: GeniusPlayerKlaverjassen,
    val analyzer: KlaverjassenAnalyzer) {

    fun mostValuableCardToPlay(): Card {
        return Card(CardColor.HEARTS, CardRank.QUEEN)
    }

    fun tryCard() {
        playerToMove.getCardsInHand().forEach { card ->
            val status = playCard(playerToMove, card)
            val value = tryRestOfGame()
            takeCardBack(playerToMove, card)
        }
    }

    fun tryRestOfGame(): Int {
        return 0
    }

    private fun playCard(player: Player, card: Card): GameStatus {
        player.removeCard(card)
        return player.game.playCard(card)
    }

    private fun takeCardBack(player: Player, card: Card) {
        player.game.takeLastCardBack()
        player.addCard(card)
    }
}