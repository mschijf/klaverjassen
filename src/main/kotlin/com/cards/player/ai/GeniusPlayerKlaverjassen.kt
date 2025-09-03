package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player

class GeniusPlayerKlaverjassen(
    tableSide: TableSide,
    game: Game) : Player(tableSide, game) {

    val analyzer = KlaverjassenAnalyzer(this)

    override fun chooseCard(): Card {
        if (getLegalPlayableCards().size == 1)
            return getLegalPlayableCards().first()

        val analysis = analyzer.refreshAnalysis()
        if (getNumberOfCardsInHand() <= 2)
            return BruteForce(this, analysis).mostValuableCardToPlay()

        return when(game.getCurrentRound().getTrickOnTable().getCardsPlayed().size) {
            0 -> LeadPlayerInTrick(this, analysis).chooseCard()
            1,2,3 -> FollowPlayerInTrick(this, analysis).chooseCard()
            else -> throw IllegalStateException("There is no such player")
        }
    }

    override fun chooseTrumpColor(cardColorOptions: List<CardColor>): CardColor {
        val trumpChoiceAnalyzer = TrumpChoiceAnalyzer(this.getCardsInHand())

        return cardColorOptions.maxBy { cardColor ->
            trumpChoiceAnalyzer.trumpChoiceValue(cardColor)
        }
    }


}
