package com.cards.player.klaverjassen.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.game.fourplayercardgame.klaverjassen.*
import com.cards.player.klaverjassen.PlayerKlaverjassen
import com.cards.tools.cardCombinations

class GeniusPlayerKlaverjassen(
    tableSide: TableSide,
    game: GameKlaverjassen
) : PlayerKlaverjassen(tableSide, game) {

    private val analyzer = KlaverjassenAnalyzer(this)

    fun printAnalyzer() {
        analyzer.refreshAnalysis()
        TableSide.values().forEach {
            val playerCanHaveCards = analyzer.playerCanHaveCards(it)
            print(String.format("%-5s ", it.toString().lowercase()))
            print(String.format("(%2d): ", playerCanHaveCards.size))
            CardColor.values().forEach { color ->
                print(String.format("%-7s: %-25s  ", color, playerCanHaveCards.filter{it.color == color}.map { it.rank.rankString }))
            }
            println()
        }
    }

    fun getCardPlayedValueList(): List<CardPlayedValue> {
        val trickSoFar = getCurrentRound().getTrickOnTable() as TrickKlaverjassen
        val legalCards = getCardsInHand().legalPlayable(trickSoFar, trump())
        val nextSide = this.tableSide.clockwiseNext()
        return legalCards.map { card ->
            trickSoFar.addCard(card)
            val v = tryPlay(trickSoFar, nextSide, false)
            trickSoFar.removeLastCard()
            CardPlayedValue(card, v.value)
        }
    }

    override fun chooseCard(): Card {
        val trumpColor = (game.getCurrentRound() as RoundKlaverjassen).getTrumpColor()
        val legalCards = getCardsInHand().legalPlayable(game.getCurrentRound().getTrickOnTable(), trumpColor)
        if (legalCards.size == 1)
            return legalCards.first()

        analyzer.refreshAnalysis()

        if (firstTrick() && isContractOwner() && isLeadPlayer() && hasTrumpJack()) {
            return trumpJack()
        }

        if (!firstPlayer()) {
            val cpvl = getCardPlayedValueList()
            val bc = cpvl.maxByOrNull { it.value }?.card?:throw Exception("empty card-played-value list")

//            print("==> $tableSide: ")
//            cpvl.forEach { cardValue ->
//                print("${cardValue.card}: ${cardValue.value}, ")
//            }
//            println()
//            println()
            return bc
        }

        // ALS IK SLAG LEADER BEN
        //  REGELS MAKEN PER 1e ronde, 2e ronde, etc.
        //     wil ik troef trekken?
        //     en kan ik troef trekken?
        //     of wil ik troef voor laatste slag bewaren?

        //ALS IK TWEEDE SPELER BEN
        //  kan follow no-trump color
        //  kan follow trump color
        //  cannot follow no-trump color but have trumps
        //  cannot follow no-trump color and have no trumps

        //ALS IK DERDE SPELER BEN
        //  kan follow no-trump color
        //  kan follow trump color
        //  cannot follow no-trump color but have trumps
        //  cannot follow no-trump color and have no trumps

        //ALS IK VIERDE SPELER BEN
        //  kan follow no-trump color
        //  kan follow trump color
        //  cannot follow no-trump color but have trumps
        //  cannot follow no-trump color and have no trumps

        return super.chooseCard()
    }

    override fun chooseTrumpColor(cardColorOptions: List<CardColor>): CardColor {
        val trumpChoiceAnalyzer = TrumpChoiceAnalyzer(this.getCardsInHand())

        return cardColorOptions.maxBy { cardColor ->
            trumpChoiceAnalyzer.trumpChoiceValue(cardColor)
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun tryPlay(trickSoFar: TrickKlaverjassen, side: TableSide, maxNode: Boolean): CardPlayedValue {
        if (trickSoFar.isComplete()) {
            return CardPlayedValue(null, trickSoFar.getScore().getDeltaForPlayer(this.tableSide))
        }

        val legalCards = if (side == this.tableSide) {
            getCardsInHand().legalPlayable(trickSoFar, trump())
        } else {
            determineCardProbabilities(side, trickSoFar).map { it.card }
        }

        var best = CardPlayedValue(null, if (maxNode) Int.MIN_VALUE else Int.MAX_VALUE)
        legalCards.forEach { card ->
            trickSoFar.addCard(card)
            val v = tryPlay(trickSoFar, side.clockwiseNext(), !maxNode)
            if (maxNode && v.isBetter(best)) {
                best = CardPlayedValue(card, v.value)
            } else if (!maxNode && v.isWorse(best)) {
                best = CardPlayedValue(card, v.value)
            }
            trickSoFar.removeLastCard()
        }
        return best
    }

    private fun determineCardProbabilities(side: TableSide, trickSoFar: TrickKlaverjassen): List<CardProbability> {
        val certainLeadColor = (analyzer.playerSureHasCards(side) - trickSoFar.getCardsPlayed())
            .filter { it.color == trickSoFar.getLeadColor() }

        val certainTrumpColor = (analyzer.playerSureHasCards(side) - trickSoFar.getCardsPlayed())
            .filter { it.color == trickSoFar.getLeadColor() }

        val probabilityValues = determineProbabilities(side, trickSoFar)
        if (certainLeadColor.isNotEmpty()) {
            val uncertainLeadColor = (analyzer.playerCanHaveCards(side)- trickSoFar.getCardsPlayed())
                .filter { it.color == trickSoFar.getLeadColor() }

            return certainLeadColor.map { CardProbability(it, 1.0) } +
                    uncertainLeadColor.map{ CardProbability(it, probabilityValues.leadColor) }

        } else if (certainTrumpColor.isNotEmpty() ) {

            val uncertainLeadColor = (analyzer.playerCanHaveCards(side)- trickSoFar.getCardsPlayed())
                .filter { it.color == trickSoFar.getLeadColor() }
            val uncertainTrumpColor = (analyzer.playerCanHaveCards(side) - trickSoFar.getCardsPlayed() - uncertainLeadColor)
                .filter { it.color == trump() }

            return uncertainLeadColor.map { CardProbability(it, probabilityValues.leadColor) } +
                   certainTrumpColor.map{ CardProbability(it, probabilityValues.trumpColor) } +
                   uncertainTrumpColor.map{ CardProbability(it, probabilityValues.trumpColor) }
        } else {
            val uncertainLeadColor = (analyzer.playerCanHaveCards(side)- trickSoFar.getCardsPlayed())
                .filter { it.color == trickSoFar.getLeadColor() }
            val uncertainTrumpColor = (analyzer.playerCanHaveCards(side) - trickSoFar.getCardsPlayed())
                .filter { it.color == trump() }
            val uncertainOtherColor = (analyzer.playerCanHaveCards(side) - trickSoFar.getCardsPlayed())
                .filter { it.color != trickSoFar.getLeadColor() && it.color != trump() }

            val certainAll = analyzer.playerSureHasCards(side) //has for sure no trump and no leadcolor
            return uncertainLeadColor.map { CardProbability(it, probabilityValues.leadColor) } +
                    uncertainTrumpColor.map{ CardProbability(it, probabilityValues.trumpColor) } +
                    certainAll.map{ CardProbability(it, probabilityValues.otherColor) } +
                    uncertainOtherColor.map{ CardProbability(it, probabilityValues.otherColor) }
        }
    }

    fun determineProbabilities(side: TableSide, trickSoFar: TrickKlaverjassen) : ProbabilityValues {
        val m = analyzer.cardsInHandForSide(side) - analyzer.playerSureHasCards(side).size
        val allSetSize = (analyzer.playerCanHaveCards(side)- trickSoFar.getCardsPlayed()).size
        val trumpSetSize = analyzer.playerCanHaveCards(side).filter { it.color == trump() }.size
        val otherSetSize = analyzer.playerCanHaveCards(side).filter { it.color != trump() && it.color != trickSoFar.getLeadColor() }.size
        val combiAll = cardCombinations(allSetSize, m)
        val combiLeadColor = cardCombinations(allSetSize, m) - cardCombinations(trumpSetSize + otherSetSize, m)
        val combiTrump = cardCombinations(trumpSetSize + otherSetSize, m) - cardCombinations(otherSetSize, m)
        val combiOther = cardCombinations(otherSetSize, m)
        return ProbabilityValues (
            leadColor = combiLeadColor.toDouble() / combiAll.toDouble(),
            trumpColor = combiTrump.toDouble() / combiAll.toDouble(),
            otherColor = combiOther.toDouble() / combiAll.toDouble()
        )
    }

    data class CardProbability(val card: Card, val probability: Double)
    data class ProbabilityValues(val leadColor: Double, val trumpColor: Double, val otherColor: Double)

    //------------------------------------------------------------------------------------------------------------------

    private fun firstPlayer() = getCurrentRound().getTrickOnTable().hasNotStarted()
    private fun secondPlayer() = getCurrentRound().getTrickOnTable().getCardsPlayed().size == 1
    private fun thirdPlayer() = getCurrentRound().getTrickOnTable().getCardsPlayed().size == 2
    private fun lastPlayer() = getCurrentRound().getTrickOnTable().getCardsPlayed().size == 3

    private fun canFollow() = getCardsInHand().any{getCurrentRound().getTrickOnTable().isLeadColor(it.color)}
    private fun hasTroef() = hasColor(trump())
    private fun mustTroeven() = !canFollow() && hasTroef()

    private fun hasColor(cardColor: CardColor) = getCardsInHand().any{it.color == cardColor}
    private fun hasCard(card: Card) = card in getCardsInHand()

    private fun firstTrick() = getCurrentRound().getTrickList().size == 1

    private fun isLeadPlayer() = getCurrentRound().getTrickOnTable().isSideToLead(this.tableSide)
    private fun isContractOwner() = getCurrentRound().isContractOwningSide(this.tableSide)
    private fun isContractOwnersPartner() = getCurrentRound().isContractOwningSide(this.tableSide.opposite())

    private fun trump() = getCurrentRound().getTrumpColor()

    private fun hasTrumpCard(rank: CardRank) = hasCard(Card(trump(), rank))
    private fun hasTrumpJack() = hasCard(trumpJack())
    private fun trumpJack() = Card(trump(), CardRank.JACK)
    private fun trumpNine() = Card(trump(), CardRank.NINE)
}

data class CardPlayedValue(val card: Card?, val value: Int) {
    fun isBetter(other: CardPlayedValue): Boolean = this.value > other.value
    fun isWorse(other: CardPlayedValue): Boolean = this.value < other.value
}
