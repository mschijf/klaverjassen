package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player

class GeniusPlayerKlaverjassen(tableSide: TableSide, game: Game) : Player(tableSide, game) {

    private fun leadColor() = game.getCurrentRound().getTrickOnTable().getLeadColor()
    private fun trump() = game.getCurrentRound().getTrumpColor()

    private fun troefGevraagdEnDieHebIkNiet() = (leadColor() == trump()) && getLegalPlayableCards().none{it.color == trump()}
    private fun troefGevraagdEnDieHebIk() = (leadColor() == trump()) && getLegalPlayableCards().any{it.color == trump()}

    private fun kleurGevraagdEnDieHebIkNietEnKanNietTroeven() = (leadColor() != trump()) && getLegalPlayableCards().none{it.color == leadColor() || it.color == trump()}
    private fun kleurGevraagdEnDieHebIkNietEnKanWelTroeven() = (leadColor() != trump()) && getLegalPlayableCards().none{it.color == leadColor()} && getLegalPlayableCards().any{it.color == trump()}
    private fun kleurGevraagdEnDieHebIk() = (leadColor() != trump()) && getLegalPlayableCards().any{it.color == leadColor()}

    private fun ikGa() = tableSide == game.getCurrentRound().getContractOwningSide()
    private fun partnerGaat() = tableSide.opposite() == game.getCurrentRound().getContractOwningSide()
    private fun opponentGaat() = !ikGa() && !partnerGaat()

    override fun chooseCard(): Card {
        if (getLegalPlayableCards().size == 1)
            return getLegalPlayableCards().first()

        if (getNumberOfCardsInHand() <= 2)
            return BruteForceRule(this).chooseCard()

        val useRule = if (game.getCurrentRound().getTrickOnTable().hasNotStarted()) {
            when {
                ikGa() -> IAmLeadPlayerIOwnContractRule(this)
                partnerGaat() -> IAmLeadPlayerPartnerOwnsContractRule(this)
                opponentGaat() -> IAmLeadPlayerTheyOwnContractRule(this)
                else -> throw Exception("main level leading rule not found")
            }
        } else {
            when {
                troefGevraagdEnDieHebIk() -> IDoHaveLeadColorAndLeadColorIsTrumpRule(this)
                troefGevraagdEnDieHebIkNiet() -> IDontHaveLeadColorNorTrumpRule(this)

                kleurGevraagdEnDieHebIk() -> IDoHaveLeadColorAndLeadColorIsNotTrumpRule(this)
                kleurGevraagdEnDieHebIkNietEnKanWelTroeven() -> IDontHaveLeadColorButDoHaveTrumpRule(this)
                kleurGevraagdEnDieHebIkNietEnKanNietTroeven() -> IDontHaveLeadColorNorTrumpRule(this)

                else -> throw Exception("main level follower rule not found")
            }
        }
        return useRule.chooseCard()
    }

    override fun chooseTrumpColor(cardColorOptions: List<CardColor>): CardColor {
        val trumpChoiceAnalyzer = TrumpChoiceAnalyzer(this.getCardsInHand())

        return cardColorOptions.maxBy { cardColor -> trumpChoiceAnalyzer.trumpChoiceValue(cardColor) }
    }

}
