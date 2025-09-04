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

    private fun leadColor() = game.getCurrentRound().getTrickOnTable().getLeadColor()
    private fun trump() = game.getCurrentRound().getTrumpColor()

    private fun ikHebGeenLeadColorEnGeenTroef() = getLegalPlayableCards().none{it.color == leadColor() || it.color == trump()}
    private fun ikHebGeenLeadColorMaarWelTroef() = getLegalPlayableCards().none{it.color == leadColor()} && getLegalPlayableCards().any{it.color == trump() }
    private fun ikHebWelLeadColorEnDatIsTroef() = leadColor() == trump() && getLegalPlayableCards().any{it.color == leadColor()}
    private fun ikHebWelLeadColorEnDatIsGeenTroef() = leadColor() != trump() && getLegalPlayableCards().any{it.color == leadColor()}

    override fun chooseCard(): Card {
        if (getLegalPlayableCards().size == 1)
            return getLegalPlayableCards().first()

        val brainDump = analyzer.refreshAnalysis()
        if (getNumberOfCardsInHand() <= 2)
            return BruteForce(this, brainDump).mostValuableCardToPlay()

        return when(game.getCurrentRound().getTrickOnTable().getCardsPlayed().size) {
            0 -> IkBenLeadPlayerRule(this, brainDump).chooseCard()
            1,2,3 -> when {
                ikHebGeenLeadColorEnGeenTroef() ->
                    IkHebGeenLeadColorEnGeenTroefRule(this, brainDump).chooseCard()
                ikHebGeenLeadColorMaarWelTroef() ->
                    IkHebGeenLeadColorMaarWelTroefRule(this, brainDump).chooseCard()
                ikHebWelLeadColorEnDatIsTroef() ->
                    IkHebWelLeadColorEnDatIsTroefRule(this, brainDump).chooseCard()
                ikHebWelLeadColorEnDatIsGeenTroef() ->
                    IkHebWelLeadColorEnDatIsGeenTroefRule(this, brainDump).chooseCard()
                else ->
                    playFallbackCard("Fall back for main level 'follow player in trick'")
            }
            else -> throw IllegalStateException("There is no such player")
        }
    }

    override fun chooseTrumpColor(cardColorOptions: List<CardColor>): CardColor {
        val trumpChoiceAnalyzer = TrumpChoiceAnalyzer(this.getCardsInHand())

        return cardColorOptions.maxBy { cardColor ->
            trumpChoiceAnalyzer.trumpChoiceValue(cardColor)
        }
    }

    protected fun playFallbackCard(info: String? = null): Card {
        if (info != null)
            println("FALL BACK NOTE: Fallback card info: $info")
        return getLegalPlayableCards().first()
    }

}
