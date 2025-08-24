package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
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
        val legalCards = getLegalPlayableCards()
        if (legalCards.size == 1)
            return legalCards.first()

        if (firstTrick() && isContractOwner() && isLeadPlayer() && hasTrumpJack()) {
            return trumpJack()
        }

        if (getNumberOfCardsInHand() == 2) {
            val analyzer = KlaverjassenAnalyzer(this)
            analyzer.refreshAnalysis()
            printAnalyzer(analyzer)
            val card = BruteForce(this, analyzer).mostValuableCardToPlay()
            return card
        }

        return super.chooseCard()
    }

    override fun chooseTrumpColor(cardColorOptions: List<CardColor>): CardColor {
        val trumpChoiceAnalyzer = TrumpChoiceAnalyzer(this.getCardsInHand())

        return cardColorOptions.maxBy { cardColor ->
            trumpChoiceAnalyzer.trumpChoiceValue(cardColor)
        }
    }

    //------------------------------------------------------------------------------------------------------------------


    //------------------------------------------------------------------------------------------------------------------

    private fun firstPlayer() = game.getCurrentRound().getTrickOnTable().hasNotStarted()
    private fun secondPlayer() = game.getCurrentRound().getTrickOnTable().getCardsPlayed().size == 1
    private fun thirdPlayer() = game.getCurrentRound().getTrickOnTable().getCardsPlayed().size == 2
    private fun lastPlayer() = game.getCurrentRound().getTrickOnTable().getCardsPlayed().size == 3

    private fun canFollow() = getCardsInHand().any{game.getCurrentRound().getTrickOnTable().isLeadColor(it.color)}
    private fun hasTroef() = hasColor(trump())
    private fun mustTroeven() = !canFollow() && hasTroef()

    private fun hasColor(cardColor: CardColor) = getCardsInHand().any{it.color == cardColor}
    private fun hasCard(card: Card) = card in getCardsInHand()

    private fun firstTrick() = game.getCurrentRound().getTrickList().size == 1

    private fun isLeadPlayer() = game.getCurrentRound().getTrickOnTable().isSideToLead(this.tableSide)
    private fun isContractOwner() = game.getCurrentRound().isContractOwningSide(this.tableSide)
    private fun isContractOwnersPartner() = game.getCurrentRound().isContractOwningSide(this.tableSide.opposite())

    private fun trump() = game.getCurrentRound().getTrumpColor()

    private fun hasTrumpCard(rank: CardRank) = hasCard(Card(trump(), rank))
    private fun hasTrumpJack() = hasCard(trumpJack())
    private fun trumpJack() = Card(trump(), CardRank.JACK)
    private fun trumpNine() = Card(trump(), CardRank.NINE)
}

data class CardPlayedValue(val card: Card?, val value: Int) {
    fun isBetter(other: CardPlayedValue): Boolean = this.value > other.value
    fun isWorse(other: CardPlayedValue): Boolean = this.value < other.value
}
