package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player

class GeniusPlayerKlaverjassen(
    tableSide: TableSide,
    game: Game) : Player(tableSide, game) {

    fun printAnalyzer(analyzer: KlaverjassenAnalyzer) {
        if (game.newRoundToBeStarted())
            return
//        val analyzer = KlaverjassenAnalyzer(this)
//        analyzer.refreshAnalysis()
        TableSide.values().forEach {
            val playerCanHaveCards = analyzer.playerCanHaveCards(it)
            print(String.format("%-5s ", it.toString().lowercase()))
            print(String.format("(%2d): ", playerCanHaveCards.size))
            CardColor.values().forEach { color ->
                print(String.format("%-8s: %-25s  ", color, playerCanHaveCards.filter{card->card.color == color}.map { card -> card.rank.rankString }))
            }
            println()
            val playerSureHasCards = analyzer.playerSureHasCards(it)
            print(String.format("%-5s ", it.toString().lowercase()))
            print(String.format("(%2d): ", playerSureHasCards.size))
            CardColor.values().forEach { color ->
                print(String.format("%-8s: %-25s  ", " ", playerSureHasCards.filter{card->card.color == color}.map { card -> card.rank.rankString }))
            }
            println()
        }
    }

    override fun chooseCard(): Card {
        if (getLegalPlayableCards().size == 1)
            return getLegalPlayableCards().first()

        if (getNumberOfCardsInHand() == 2)
            return BruteForce(this).mostValuableCardToPlay()

        return when(game.getCurrentRound().getTrickOnTable().getCardsPlayed().size) {
            0 -> FirstPlayerInTrick(this).chooseCard()
            1 -> SecondPlayerInTrick(this).chooseCard()
            2 -> ThirdPlayerInTrick(this).chooseCard()
            3 -> FourthPlayerInTrick(this).chooseCard()
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
