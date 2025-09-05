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

    private fun troefGevraagdEnDieHebIkNiet() = (leadColor() == trump()) && getLegalPlayableCards().none{it.color == trump()}
    private fun troefGevraagdEnIkKanVolgen() = (leadColor() == trump()) && getLegalPlayableCards().any{it.color == trump()}
    private fun kleurGevraagdEnDieHebIkNietEnKanNietTroeven() = (leadColor() != trump()) && getLegalPlayableCards().none{it.color == leadColor() || it.color == trump()}
    private fun kleurGevraagdEnDieHebIkNietEnKanWelTroeven() = (leadColor() != trump()) && getLegalPlayableCards().none{it.color == leadColor()} && getLegalPlayableCards().any{it.color == trump()}
    private fun kleurGevraagdEnDieHebIkWel() = (leadColor() != trump()) && getLegalPlayableCards().any{it.color == leadColor()}

    override fun chooseCard(): Card {
        if (getLegalPlayableCards().size == 1)
            return getLegalPlayableCards().first()

        val brainDump = analyzer.refreshAnalysis()
        if (getNumberOfCardsInHand() <= 2)
            return BruteForce(this, brainDump).mostValuableCardToPlay()

        return when(game.getCurrentRound().getTrickOnTable().getCardsPlayed().size) {
            0 -> IkBenLeadPlayerRule(this, brainDump).chooseCard()
            1,
            2,
            3 -> when {
                troefGevraagdEnDieHebIkNiet() ->
                    IkHebGeenLeadColorEnGeenTroefRule(this, brainDump).chooseCard()

                troefGevraagdEnIkKanVolgen() ->
                    TroefGevraagdEnIkKanVolgenRule(this, brainDump).chooseCard()

                kleurGevraagdEnDieHebIkNietEnKanNietTroeven() ->
                    IkHebGeenLeadColorEnGeenTroefRule(this, brainDump).chooseCard()

                kleurGevraagdEnDieHebIkNietEnKanWelTroeven() ->
                    IkHebGeenLeadColorMaarWelTroefRule(this, brainDump).chooseCard()

                kleurGevraagdEnDieHebIkWel() ->
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
